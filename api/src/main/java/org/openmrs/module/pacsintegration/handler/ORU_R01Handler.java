package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Provider;
import org.openmrs.module.pacsintegration.util.HL7Utils;
import org.openmrs.module.radiologyapp.RadiologyReport;
import org.openmrs.util.OpenmrsUtil;

import java.util.List;

public class ORU_R01Handler extends HL7Handler {

    public ORU_R01Handler() {
    }

    @Override
    HL7HandlerTask getHL7HandlerTask(final Message message) {
        return new HL7HandlerTask() {
            @Override
            public void run() {
                try {
                    ORU_R01 oruR01 = (ORU_R01) message;
                    String messageControlID = oruR01.getMSH().getMessageControlID().getValue();
                    String sendingFacility = null;

                    try {
                        sendingFacility = getSendingFacility();

                        String patientIdentifier = oruR01.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();

                        RadiologyReport report = new RadiologyReport();

                        report.setOrderNumber(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue());
                        report.setPatient(getPatient(patientIdentifier));
                        report.setReportDate(syncTimeWithCurrentServerTime(HL7Utils.getHl7DateFormat().parse(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR()
                                .getObservationDateTime().getTimeOfAnEvent().getValue())));
                        report.setAssociatedRadiologyOrder(getRadiologyOrder(report.getOrderNumber(), report.getPatient()));
                        report.setProcedure(getProcedure(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue()));
                        report.setPrincipalResultsInterpreter(getResultsInterpreter(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getPrincipalResultInterpreter().getOPName().getIDNumber().getValue()));
                        report.setReportType(getReportType(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getResultStatus().getValue()));
                        report.setReportLocation(getLocationByName(oruR01.getMSH().getSendingFacility().getNamespaceID().getValue()));
                        report.setReportBody(getReportBody(oruR01.getRESPONSE().getORDER_OBSERVATION()));

                        if (!isDuplicate(report)) {
                            radiologyService.saveRadiologyReport(report);
                        }
                        else {
                            log.warn("Duplicate report for order number " + report.getOrderNumber() + ", not saving");
                        }
                    }
                    catch (Exception e) {
                        log.error("Unable to parse incoming ORU_RO1 message", e);
                        resultMessage = HL7Utils.generateErrorACK(messageControlID, sendingFacility, e.getMessage());
                    }
                    resultMessage = HL7Utils.generateACK(oruR01.getMSH().getMessageControlID().getValue(), sendingFacility);
                }
                catch (HL7Exception hl7e) {
                    hl7Exception = hl7e;
                }
            }
        };
    }

    @Override
    public boolean canProcess(Message message) {
        return  message != null && "ORU_R01".equals(message.getName());
    }

    // test for duplicates, which can happen when PACS sends the same message twice; we consider a report
    // a duplicate if order number, report type, principal results interpreter, date and body are all the same
    // (we shouldn't need to check patient or study type, since we have checked order number)
    private Boolean isDuplicate(RadiologyReport report) {
        List<RadiologyReport> reports = radiologyService.getRadiologyReportsByOrderNumber(report.getOrderNumber());  // TODO obviously, change this to not be hardcoded!!!
        if (reports == null || reports.size() == 0) {
            return false;
        }
        else {
            for (RadiologyReport r : reports) {
                if (OpenmrsUtil.nullSafeEquals(r.getReportType(), report.getReportType()) &&
                        OpenmrsUtil.nullSafeEquals(r.getPrincipalResultsInterpreter(), report.getPrincipalResultsInterpreter()) &&
                        OpenmrsUtil.nullSafeEquals(r.getReportBody().trim(), report.getReportBody().trim()) &&
                        OpenmrsUtil.compare(report.getReportDate(), r.getReportDate()) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getReportBody(ORU_R01_ORDER_OBSERVATION obsSet) throws HL7Exception {
        StringBuffer reportBody = new StringBuffer();

       for (int i = 0; i < obsSet.getOBSERVATIONReps(); i++) {

            ORU_R01_OBSERVATION obs = obsSet.getOBSERVATION(i);

            if (obs.getOBX().getObservationValue().length > 0 &&
                    obs.getOBX().getObservationValue()[0].getData() != null &&
                    obs.getOBX().getObservationValue()[0].getData().toString() != null) {
                reportBody.append(obs.getOBX().getObservationValue()[0].getData().toString());
            }

            reportBody.append("\r\n");
        }

        return reportBody.toString();
    }


    private Provider getResultsInterpreter(String providerId) {
        if (StringUtils.isNotBlank(providerId)) {
            return providerService.getProviderByIdentifier(providerId);
        }

        return null;
    }

    private Concept getReportType(String reportCode) {
        if (StringUtils.isNotBlank(reportCode)) {
            if (reportCode.equalsIgnoreCase("P"))  {
                return pacsIntegrationProperties.getReportTypePrelimConcept();
            }
            else if (reportCode.equalsIgnoreCase("F")) {
                return pacsIntegrationProperties.getReportTypeFinalConcept();
            }
            else if (reportCode.equalsIgnoreCase("C") || reportCode.equalsIgnoreCase("A"))  {
                return pacsIntegrationProperties.getReportTypeCorrectionConcept();
            }
        }

        return null;
    }
}
