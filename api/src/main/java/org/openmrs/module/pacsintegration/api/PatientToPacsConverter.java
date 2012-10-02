package org.openmrs.module.pacsintegration.api;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.AbstractMessage;
import ca.uhn.hl7v2.model.v23.message.ADT_A01;
import ca.uhn.hl7v2.model.v23.message.ADT_A08;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.model.v23.segment.PV1;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.openmrs.Patient;

import java.text.SimpleDateFormat;

public class PatientToPacsConverter {

    private final SimpleDateFormat pacsDateFormat = new SimpleDateFormat("yyyyMMddHHmm");;

    private Parser parser = new PipeParser();

    public PatientToPacsConverter() {
    }

    public String convertToPacsFormat(Patient patient, String messageType) throws HL7Exception {

        AbstractMessage adtMessage;
        MSH mshSegment;
        PID pidSegment;
        PV1 pv1Segment;

        if (messageType.equals("A01")) {
            adtMessage = new ADT_A01();
            mshSegment = ((ADT_A01) adtMessage).getMSH();
            pidSegment = ((ADT_A01) adtMessage).getPID();
            pv1Segment = ((ADT_A01) adtMessage).getPV1();
        }
        else if (messageType.equals("A08")) {
            adtMessage = new ADT_A08();
            mshSegment = ((ADT_A08) adtMessage).getMSH();
            pidSegment = ((ADT_A08) adtMessage).getPID();
            pv1Segment = ((ADT_A08) adtMessage).getPV1();
        }
        else {
            throw new RuntimeException("Unsupported ADT Message type");
        }

        // Populate the MSH Segment
        mshSegment.getFieldSeparator().setValue("|");
        mshSegment.getEncodingCharacters().setValue("^~\\&");
        mshSegment.getMessageType().getMessageType().setValue("ADT");
        mshSegment.getMessageType().getTriggerEvent().setValue(messageType);
        mshSegment.getProcessingID().getProcessingID().setValue("P");  // stands for production (?)
        mshSegment.getVersionID().setValue("2.3");
        // TODO: add sending facility

        // Populate the PID Segment
        pidSegment.getPatientIDInternalID(0).getID().setValue(patient.getPatientIdentifier().getIdentifier());
        pidSegment.getPatientName().getFamilyName().setValue(patient.getFamilyName());
        pidSegment.getPatientName().getGivenName().setValue(patient.getGivenName());
        pidSegment.getDateOfBirth().getTimeOfAnEvent().setValue(pacsDateFormat.format(patient.getBirthdate()));
        pidSegment.getSex().setValue(patient.getGender());

        // Populate the PV1 Segment
        // TODO: add patient class
        // TODO: add assigned patient location

        return parser.encode(adtMessage);
    }

}
