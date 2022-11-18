package org.openmrs.module.pacsintegration.incoming;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v23.datatype.CN;
import ca.uhn.hl7v2.model.v23.datatype.TS;
import ca.uhn.hl7v2.model.v23.datatype.TX;
import ca.uhn.hl7v2.model.v23.group.ORM_O01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORM_O01_ORDER_DETAIL;
import ca.uhn.hl7v2.model.v23.message.ORM_O01;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.util.HL7Utils;
import org.openmrs.module.radiologyapp.RadiologyStudy;

import java.text.ParseException;
import java.util.Date;

public class ORM_O01Handler extends IncomingMessageHandler {

    @Override
    IncomingMessageTask getHL7Task(final Message message) {
        return new IncomingMessageTask() {
            @Override
            public void run() {
                try {
                    setIncomingMessage(message);
                    ORM_O01 ormO01 = (ORM_O01) message;
                    String messageControlID = ormO01.getMSH().getMessageControlID().getValue();
                    String sendingFacility = null;

                    try {
                        sendingFacility = getSendingFacility();

                        String eventType = getEventType(ormO01.getORDER().getORDER_DETAIL());

                        // we are triggering the create of a RadiologyStudy on reception of a "Reviewed" event, which
                        // means that the technologist has marked the study as reviewed, or a "Reported" event, which
                        // means that the study has been reported on, or a "Needs Overread" event which means that an
                        // overread from Boston is requested;
                        // we only create an study once, so if multiple REVIEWED/REPORTED/NEEDS OVERREAD
                        // events are received (likely) it won't create a dup or throw and error

                        if (StringUtils.isNotBlank(eventType) &&
                                (eventType.equalsIgnoreCase("REVIEWED") || eventType.equalsIgnoreCase("REPORTED")
                                        || eventType.equalsIgnoreCase("NEEDSOVERREAD"))) {

                            String orderNumber = ormO01.getORDER().getORDER_DETAIL().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue();

                            // only create this study if it doesn't already exist
                            if (radiologyService.getRadiologyStudyByOrderNumber(orderNumber) == null) {

                                String patientIdentifier = ormO01.getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();

                                RadiologyStudy radiologyStudy = new RadiologyStudy();

                                radiologyStudy.setPatient(getPatient(patientIdentifier));
                                radiologyStudy.setOrderNumber(orderNumber);
                                radiologyStudy.setAssociatedRadiologyOrder(getRadiologyOrder(radiologyStudy.getOrderNumber(), radiologyStudy.getPatient()));
                                radiologyStudy.setProcedure(getProcedure(ormO01.getORDER().getORDER_DETAIL().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue()));
                                radiologyStudy.setTechnician(getTechnologist(ormO01.getORDER().getORDER_DETAIL()));
                                radiologyStudy.setDatePerformed(syncTimeWithCurrentServerTime(getDatePerformed(ormO01.getORDER().getORDER_DETAIL())));
                                radiologyStudy.setImagesAvailable(areImagesAvailable(ormO01.getORDER().getORDER_DETAIL()));
                                radiologyStudy.setStudyLocation(getLocationByName(ormO01.getMSH().getSendingFacility().getNamespaceID().getValue()));

                                radiologyService.saveRadiologyStudy(radiologyStudy);
                            }
                        }
                        setResultMessage(HL7Utils.generateACK(ormO01.getMSH().getMessageControlID().getValue(), sendingFacility));
                    }
                    catch (Exception e) {
                        log.error("Unable to parse incoming ORM_OO1 message", e);
                        setResultMessage(HL7Utils.generateErrorACK(messageControlID, sendingFacility, e.getMessage()));
                    }
                }
                catch (HL7Exception hl7e) {
                    setHl7Exception(hl7e);
                }
            }
        };
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
                return HL7Utils.getHl7DateFormat().parse(datePerformed);
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
}
