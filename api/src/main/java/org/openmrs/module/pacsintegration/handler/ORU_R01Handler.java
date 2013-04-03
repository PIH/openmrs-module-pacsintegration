package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.emr.api.EmrService;
import org.openmrs.module.emr.radiology.RadiologyOrder;
import org.openmrs.module.emr.radiology.RadiologyReport;
import org.openmrs.module.pacsintegration.PacsIntegrationException;
import org.openmrs.module.pacsintegration.util.HL7Utils;

import java.text.ParseException;
import java.util.Date;

public class ORU_R01Handler extends HL7Handler implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());

    private ProviderService providerService;

    public ORU_R01Handler() {
    }

    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {

        ORU_R01 oruR01 = (ORU_R01) message;
        String messageControlID = oruR01.getMSH().getMessageControlID().getValue();

        try {
            String patientIdentifier = oruR01.getRESPONSE().getPATIENT().getPID().getPatientIDInternalID(0).getID().getValue();

            RadiologyReport report = new RadiologyReport();

            report.setAccessionNumber(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getFillerOrderNumber().getEntityIdentifier().getValue());
            report.setPatient(getPatient(patientIdentifier));
            report.setReportDate(HL7Utils.hl7DateFormat.parse(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR()
                    .getObservationDateTime().getTimeOfAnEvent().getValue()));
            report.setAssociatedRadiologyOrder(getRadiologyOrder(report.getAccessionNumber(), report.getPatient()));
            report.setProcedure(getProcedure(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getUniversalServiceIdentifier().getIdentifier().getValue()));
            report.setPrincipalResultsInterpreter(getResultsInterpreter(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getPrincipalResultInterpreter().getOPName().getIDNumber().getValue()));
            report.setReportType(getReportType(oruR01.getRESPONSE().getORDER_OBSERVATION().getOBR().getResultStatus().getValue()));
            report.setReportLocation(getLocationByName(oruR01.getMSH().getSendingFacility().getNamespaceID().getValue()));
            report.setReportBody(getReportBody(oruR01.getRESPONSE().getORDER_OBSERVATION()));

            radiologyService.saveRadiologyReport(report);
        }

        catch (Exception e) {
            log.error(e.getMessage());
            return HL7Utils.generateErrorACK(messageControlID, getSendingFacility(),
                    e.getMessage());
        }

        return HL7Utils.generateACK(oruR01.getMSH().getMessageControlID().getValue(), getSendingFacility());
    }

    @Override
    public boolean canProcess(Message message) {
        return message != null && "ORM_O01".equals(message.getName());
    }

    private String getReportBody(ORU_R01_ORDER_OBSERVATION obsSet) throws HL7Exception {
        StringBuffer reportBody = new StringBuffer();

       for (int i = 0; i < obsSet.getOBSERVATIONReps(); i++) {

            ORU_R01_OBSERVATION obs = obsSet.getOBSERVATION(i);

            if (obs.getOBX().getObservationValue().length > 0 &&
                    obs.getOBX().getObservationValue()[0].getData() != null) {
                reportBody.append(obs.getOBX().getObservationValue()[0].getData().toString());
            }

            reportBody.append("\r\n");
        }

        return reportBody.toString();
    }


    private Provider getResultsInterpreter(String providerId) {
        return providerService.getProviderByIdentifier(providerId);
    }

    private RadiologyReport.Type getReportType(String reportCode) {
        if (reportCode.equalsIgnoreCase("P"))  {
            return RadiologyReport.Type.PRELIM;
        }
        if (reportCode.equalsIgnoreCase("F")) {
            return RadiologyReport.Type.FINAL;
        }
        if (reportCode.equalsIgnoreCase("C"))  {
            return RadiologyReport.Type.CORRECTION;
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
