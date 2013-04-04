package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v23.datatype.CN;
import ca.uhn.hl7v2.model.v23.datatype.TS;
import ca.uhn.hl7v2.model.v23.datatype.TX;
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
import org.openmrs.module.emr.radiology.RadiologyStudy;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
import org.openmrs.module.pacsintegration.util.HL7Utils;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ORM_O01Handler extends HL7Handler implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());;

    private ProviderService providerService;

    // TODO: do we want to assume that this "stream" (via port 6662) is only from PACS


    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {

        ORM_O01 ormO01 = (ORM_O01) message;
        String messageControlID = ormO01.getMSH().getMessageControlID().getValue();

        try {

            String eventType = getEventType(ormO01.getORDER().getORDER_DETAIL());

            // we are triggering the create of a RadiologyStudy on reception of a "Reviewed" event, which
            // means that the technologist has marked the study as reviewed
            if (StringUtils.isNotBlank(eventType) && eventType.equalsIgnoreCase("REVIEWED")) {

                String patientIdentifier = ormO01.getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();

                RadiologyStudy radiologyStudy = new RadiologyStudy();

                radiologyStudy.setPatient(getPatient(patientIdentifier));
                radiologyStudy.setAccessionNumber(ormO01.getORDER().getORDER_DETAIL().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue());
                radiologyStudy.setAssociatedRadiologyOrder(getRadiologyOrder(radiologyStudy.getAccessionNumber(), radiologyStudy.getPatient()));
                radiologyStudy.setProcedure(getProcedure(ormO01.getORDER().getORDER_DETAIL().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue()));
                radiologyStudy.setTechnician(getTechnologist(ormO01.getORDER().getORDER_DETAIL()));
                radiologyStudy.setDatePerformed(getDatePerformed(ormO01.getORDER().getORDER_DETAIL()));
                radiologyStudy.setImagesAvailable(areImagesAvailable(ormO01.getORDER().getORDER_DETAIL()));

                radiologyService.saveRadiologyStudy(radiologyStudy);
            }
        }
        catch (Exception e) {
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
        Type eventType = getFieldData(orderDetail, "EventType");
        return eventType != null ? eventType.toString() : null;
    }

    protected Provider getTechnologist(ORM_O01_ORDER_DETAIL orderDetail) throws HL7Exception {

        Type provider = getFieldData(orderDetail, "Technologist");

        if (provider != null && ((CN) provider).getIDNumber() != null) {
            String techProviderId = ((CN) provider).getIDNumber().getValue();
            if (StringUtils.isNotBlank(techProviderId)) {
                return providerService.getProviderByIdentifier(techProviderId);
            }
        }

        return null;
    }

    protected Date getDatePerformed(ORM_O01_ORDER_DETAIL orderDetail) throws HL7Exception, ParseException {

        try {
            String datePerformed = ((TS) getFieldData(orderDetail, "StartDateTime")).getTimeOfAnEvent().getValue();

            if (StringUtils.isNotBlank(datePerformed)) {
                return HL7Utils.hl7DateFormat.parse(datePerformed);
            }
            else {
                throw new PacsIntegrationException("Cannot import message. Date performed must be specified.");
            }
        }
        catch (Exception e) {
            throw new PacsIntegrationException("Cannot import message. Date performed must be specified.", e);
        }

    }

    protected Boolean areImagesAvailable(ORM_O01_ORDER_DETAIL orderDetail) throws HL7Exception {
        Type imagesAvailable = getFieldData(orderDetail, "ImagesAvailable");

        if (imagesAvailable != null && ((TX) imagesAvailable).getValue() != null) {
            return  "1".equals(((TX) imagesAvailable).getValue());
        }

        return null;
    }

    protected Type getFieldData(ORM_O01_ORDER_DETAIL orderDetail, String field) throws HL7Exception  {
        // iterate through all the obx fields, looking for "specified field"
        for (int i = 0; i < orderDetail.getOBSERVATIONReps(); i++) {
            ORM_O01_OBSERVATION obs = orderDetail.getOBSERVATION(i);
            if (obs.getOBX().getObservationIdentifier().getIdentifier().getValue() != null &&
                    obs.getOBX().getObservationIdentifier().getIdentifier().getValue().equalsIgnoreCase(field)) {
                return obs.getOBX().getObservationValue(0).getData();
            }
        }
        return null;
    }


    /**
     * Setters
     */

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }
}
