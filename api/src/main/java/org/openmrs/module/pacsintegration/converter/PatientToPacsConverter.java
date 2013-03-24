package org.openmrs.module.pacsintegration.converter;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.message.ADT_A01;
import ca.uhn.hl7v2.model.v23.message.ADT_A08;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties;

import java.text.SimpleDateFormat;

public class PatientToPacsConverter {

    // TODO: not currently used

    private final SimpleDateFormat pacsDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    private Parser parser = new PipeParser();

    private PatientService patientService;

    private AdministrationService adminService;

    public String convertToAdmitMessage(Patient patient) throws HL7Exception {
        ADT_A01 message = new ADT_A01();
        populateMSHSegment(message.getMSH(), "A01");
        populatePIDSegment(message.getPID(), patient);

        // Populate the PV1 Segment
        // TODO: add patient class
        // TODO: add assigned patient location

        return parser.encode(message);  //To change body of created methods use File | Settings | File Templates.
    }

    public String convertToUpdateMessage(Patient patient) throws HL7Exception {
        ADT_A08 message = new ADT_A08();
        populateMSHSegment(message.getMSH(), "A08");
        populatePIDSegment(message.getPID(), patient);

        // Populate the PV1 Segment
        // TODO: add patient class
        // TODO: add assigned patient location

        return parser.encode(message);
    }


    private void populateMSHSegment(MSH mshSegment, String messageType) throws DataTypeException {
        // TODO: refactor to use the HL7Util method
        mshSegment.getFieldSeparator().setValue("|");
        mshSegment.getEncodingCharacters().setValue("^~\\&");
        mshSegment.getMessageType().getMessageType().setValue("ADT");
        mshSegment.getMessageType().getTriggerEvent().setValue(messageType);
        mshSegment.getProcessingID().getProcessingID().setValue("P");  // stands for production (?)
        mshSegment.getVersionID().setValue("2.3");
        // TODO: add sending facility
    }

    private void populatePIDSegment(PID pidSegment, Patient patient) throws HL7Exception {
        // Populate the PID Segment
        pidSegment.getPatientIDInternalID(0).getID().setValue(patient.getPatientIdentifier(getPatientIdentifierType()).getIdentifier());
        pidSegment.getPatientName().getFamilyName().setValue(patient.getFamilyName());
        pidSegment.getPatientName().getGivenName().setValue(patient.getGivenName());
        pidSegment.getDateOfBirth().getTimeOfAnEvent().setValue(pacsDateFormat.format(patient.getBirthdate()));
        pidSegment.getSex().setValue(patient.getGender());
    }

    private PatientIdentifierType getPatientIdentifierType() {
        PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid(adminService.getGlobalProperty(PacsIntegrationGlobalProperties.PATIENT_IDENTIFIER_TYPE_UUID));
        if (patientIdentifierType == null) {
            throw new RuntimeException("No patient identifier type specified. Is pacsintegration.patientIdentifierTypeUuid properly set?");
        }
        else {
            return patientIdentifierType;
        }
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }
}
