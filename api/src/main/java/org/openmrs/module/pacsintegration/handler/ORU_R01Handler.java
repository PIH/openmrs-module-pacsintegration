package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emr.radiology.RadiologyOrder;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties;
import org.openmrs.module.pacsintegration.util.HL7Utils;

import java.util.Collections;
import java.util.List;

public class ORU_R01Handler implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());

    private PatientService patientService;

    private AdministrationService adminService;

    private EmrProperties emrProperties;

    // TODO: can you have multiple application handlers for different messages/triggers?
    // TODO: do we want to assume that this "stream" (via port 6662) is only from PACS
    // TODO: how does "can process" work?  should only handle records from a certain source?

    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {

        ORU_R01 oruR01 = (ORU_R01) message;
        String messageControlID = oruR01.getMSH().getMessageControlID().getValue();
        String patientIdentifier = oruR01.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();
        String accessionNumber = oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue();

        // TODO: do I still need to validate the accession number?

        try {
           Patient patient = getPatient(patientIdentifier);
            // TODO: create radiology order dao method in EMR module?
           //RadiologyOrder order = getRadiologyOrder(accessionNumber, patient);

            // now check if encounter exists, if not crate
            // now getEncounterOrCreateIfNecessary--see if there are any study encounters associated with this order
            // set provider for the order if none specified
            // then parse and create any report object


        }
        catch (PacsIntegrationException e) {
            log.error(e.getMessage());
            return HL7Utils.generateErrorACK(messageControlID, getSendingFacility(),
                    e.getMessage());
        }

        return HL7Utils.generateACK(oruR01.getMSH().getMessageControlID().getValue(),
                adminService.getGlobalProperty(PacsIntegrationGlobalProperties.SENDING_FACILITY));
    }

    @Override
    public boolean canProcess(Message message) {
        return message != null && "ORU_R01".equals(message.getName());
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setEmrProperties(EmrProperties emrProperties) {
        this.emrProperties = emrProperties;
    }

    private Patient getPatient(String patientIdentifier) {
        if (StringUtils.isBlank(patientIdentifier)) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. No patient identifier specified.");
        }

        List<Patient> patientList = patientService.getPatients(null, patientIdentifier,
                Collections.singletonList(emrProperties.getPrimaryIdentifierType()), true);

        if (patientList == null || patientList.size() == 0) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. No patient with identifier " + patientIdentifier);
        }

        if (patientList.size() > 1) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. Multiple patients with identifier " + patientIdentifier);
        }

        return patientList.get(0);
    }

    private void validateAccessionNumber(String accessionNumber, Patient patient) {
        if (StringUtils.isBlank(accessionNumber)) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. No accession number specified.");
        }


    }

    private String getSendingFacility() {
        return adminService.getGlobalProperty(PacsIntegrationGlobalProperties.SENDING_FACILITY);
    }
}
