package org.openmrs.module.pacsintegration.api;

import com.thoughtworks.xstream.XStream;
import org.openmrs.Patient;

import java.text.SimpleDateFormat;

public class PatientToPacsConverter {

    private XStream xStream = new XStream();

    public PatientToPacsConverter() {
        xStream.omitField(PatientToPacsConverter.class, "xStream");  // hack to avoid XStream error - Cannot marshal the XStream instance in action
        xStream.alias("ADTMessage", ADTMessage.class);
    }

    public String convertToPacsFormat(Patient patient, String sendingFacility) {
        ADTMessage adtMessage = new ADTMessage(patient, sendingFacility);
        return xStream.toXML(adtMessage);
    }


    private class ADTMessage {
        private final SimpleDateFormat pacsDateFormat = new SimpleDateFormat("yyyyMMddHHmm");;

        private String sendingFacility;
        private String patientIdentifier;
        private String familyName;
        private String givenName;
        private String dateOfBirth;
        private String patientSex;

        public ADTMessage(Patient patient, String sendingFacility) {
            this.sendingFacility = sendingFacility;
            this.patientIdentifier = patient.getPatientIdentifier().getIdentifier();
            this.familyName = patient.getFamilyName();
            this.givenName = patient.getGivenName();
            this.dateOfBirth = pacsDateFormat.format(patient.getBirthdate());
            this.patientSex = patient.getGender();
        }
    }
}
