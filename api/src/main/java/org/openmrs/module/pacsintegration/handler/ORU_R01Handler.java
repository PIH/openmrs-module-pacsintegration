package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.emr.EmrProperties;
import org.openmrs.module.emr.radiology.RadiologyOrder;
import org.openmrs.module.emr.radiology.RadiologyService;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
import org.openmrs.module.pacsintegration.util.HL7Utils;

import java.util.Collections;
import java.util.List;

public class ORU_R01Handler implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());

    private PatientService patientService;

    private EncounterService encounterService;

    private ConceptService conceptService;

    private AdministrationService adminService;

    private RadiologyService radiologyService;

    private ProviderService providerService;

    private EmrProperties emrProperties;

    private PacsIntegrationProperties pacsIntegrationProperties;

    // TODO: do we want to assume that this "stream" (via port 6662) is only from PACS

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public void setRadiologyService(RadiologyService radiologyService) {
        this.radiologyService = radiologyService;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public void setEmrProperties(EmrProperties emrProperties) {
        this.emrProperties = emrProperties;
    }

    public void setPacsIntegrationProperties(PacsIntegrationProperties pacsIntegrationProperties) {
        this.pacsIntegrationProperties = pacsIntegrationProperties;
    }

    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {

        ORU_R01 oruR01 = (ORU_R01) message;
        String messageControlID = oruR01.getMSH().getMessageControlID().getValue();
        String patientIdentifier = oruR01.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();
        String accessionNumber = oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue();
        String encounterDate = oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getObservationDateTime().getTimeOfAnEvent().getValue();  // TODO: parse this


        Encounter encounter = new Encounter();

        try {
            Patient patient = getPatient(patientIdentifier);
            RadiologyOrder order = getRadiologyOrder(accessionNumber, patient);
            Concept procedure = getProcedure(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue());

            String eventType = getEventType(oruR01.getRESPONSE().getORDER_OBSERVATION());

            if (StringUtils.isNotBlank(eventType) && eventType.equalsIgnoreCase("StudyComplete")) {
                Provider technologist = getTechnologist(oruR01.getRESPONSE().getORDER_OBSERVATION());

                encounterService.saveEncounter(encounter);
            }
            else {
                // right now we aren't handling any other cases besides the study complete case
            }

        }
        catch (PacsIntegrationException e) {
            log.error(e.getMessage());
            return HL7Utils.generateErrorACK(messageControlID, getSendingFacility(),
                    e.getMessage());
        }

        return HL7Utils.generateACK(oruR01.getMSH().getMessageControlID().getValue(),
                adminService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY));
    }

    @Override
    public boolean canProcess(Message message) {
        return message != null && "ORU_R01".equals(message.getName());
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

    private RadiologyOrder getRadiologyOrder(String accessionNumber, Patient patient) {

        // if no accession number, no order to associate with
        if (StringUtils.isBlank(accessionNumber)) {
            return null;
        }

        // try to fetch the order by accession number
        RadiologyOrder radiologyOrder = radiologyService.getRadiologyOrderByAccessionNumber(accessionNumber);

        // if there is an existing order, make sure the patients match
        // (note that we are allowing incoming obs with accession numbers that don't match anything in the system)
        if (radiologyOrder != null && !radiologyOrder.getPatient().equals(patient)) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. Patient referenced in message different from patient attached to existing order.");

        }

        return radiologyOrder;
    }

    private String getEventType(ORU_R01_ORDER_OBSERVATION orderObs) throws HL7Exception {
        return getFieldData(orderObs, "EventType");
    }

    private Provider getTechnologist(ORU_R01_ORDER_OBSERVATION orderObs) throws HL7Exception {

        String techInfo = getFieldData(orderObs, "Technologist");

        if (StringUtils.isNotBlank(techInfo)) {
            String techProviderId = techInfo.split("\\^")[0];
            if (StringUtils.isNotBlank(techProviderId)) {
                return providerService.getProviderByIdentifier(techProviderId);
            }
        }

        return null;
    }

    private Concept getProcedure(String procedureCode) {
        Concept procedure = conceptService.getConceptByMapping(procedureCode, pacsIntegrationProperties.getProcedureCodesConceptSource().getName());

        if (procedure == null) {
            throw new PacsIntegrationException("Cannot import ORU_R01 message. Procedure code not recognized.");
        }

        return procedure;
    }

    private String getSendingFacility() {
        return adminService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY);
    }

    private String getFieldData(ORU_R01_ORDER_OBSERVATION orderObs, String field) throws HL7Exception  {
        // iterate through all the obx fields, looking for "specified field"
        for (int i = 0; i < orderObs.getOBSERVATIONReps(); i++) {
            ORU_R01_OBSERVATION obs = orderObs.getOBSERVATION(i);
            if (obs.getOBX().getObservationIdentifier().getIdentifier().getValue() != null &&
                    obs.getOBX().getObservationIdentifier().getIdentifier().getValue().equalsIgnoreCase(field)) {
                return obs.getOBX().getObservationValue(0).getData().toString();
            }
        }
        return null;
    }
}
