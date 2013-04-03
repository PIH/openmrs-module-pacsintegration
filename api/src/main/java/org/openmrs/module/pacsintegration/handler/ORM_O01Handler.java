package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.group.ORM_O01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORM_O01_ORDER_DETAIL;
import ca.uhn.hl7v2.model.v23.message.ORM_O01;
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

public class ORM_O01Handler extends HL7Handler implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());

    private EncounterService encounterService;

    private ProviderService providerService;

    // TODO: do we want to assume that this "stream" (via port 6662) is only from PACS


    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {

        ORM_O01 ormO01 = (ORM_O01) message;
        String messageControlID = ormO01.getMSH().getMessageControlID().getValue();

        try {

            String patientIdentifier = ormO01.getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();
            String accessionNumber = ormO01.getORDER().getORDER_DETAIL().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue();
            String encounterDate = ormO01.getORDER().getORDER_DETAIL().getOBR().getObservationDateTime().getTimeOfAnEvent().getValue();  // TODO: parse this

            Patient patient = getPatient(patientIdentifier);
            RadiologyOrder order = getRadiologyOrder(accessionNumber, patient);
            Concept procedure = getProcedure(ormO01.getORDER().getORDER_DETAIL().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue());

            String eventType = getEventType(ormO01.getORDER().getORDER_DETAIL());

            if (StringUtils.isNotBlank(eventType) && eventType.equalsIgnoreCase("StudyComplete")) {
                Provider technologist = getTechnologist(ormO01.getORDER().getORDER_DETAIL());

                Encounter encounter = new Encounter();

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

        return HL7Utils.generateACK(ormO01.getMSH().getMessageControlID().getValue(), getSendingFacility());
    }

    @Override
    public boolean canProcess(Message message) {
        return message != null && "ORM_O01".equals(message.getName());
    }

    protected String getEventType(ORM_O01_ORDER_DETAIL orderDetail) throws HL7Exception {
        return getFieldData(orderDetail, "EventType");
    }

    protected Provider getTechnologist(ORM_O01_ORDER_DETAIL orderDetail) throws HL7Exception {

        String techInfo = getFieldData(orderDetail, "Technologist");

        if (StringUtils.isNotBlank(techInfo)) {
            String techProviderId = techInfo.split("\\^")[0];
            if (StringUtils.isNotBlank(techProviderId)) {
                return providerService.getProviderByIdentifier(techProviderId);
            }
        }

        return null;
    }

    protected String getFieldData(ORM_O01_ORDER_DETAIL orderDetail, String field) throws HL7Exception  {
        // iterate through all the obx fields, looking for "specified field"
        for (int i = 0; i < orderDetail.getOBSERVATIONReps(); i++) {
            ORM_O01_OBSERVATION obs = orderDetail.getOBSERVATION(i);
            if (obs.getOBX().getObservationIdentifier().getIdentifier().getValue() != null &&
                    obs.getOBX().getObservationIdentifier().getIdentifier().getValue().equalsIgnoreCase(field)) {
                return obs.getOBX().getObservationValue(0).getData().toString();
            }
        }
        return null;
    }


    /**
     * Setters
     */

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }
}
