package org.openmrs.module.pacsintegration.api;

import ca.uhn.hl7v2.HL7Exception;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class PatientToPacsConverterTest {

    private PatientToPacsConverter converter;

    @Before
    public void setup() {
        converter = new PatientToPacsConverter();
    }

    @Test
    public void shouldGeneratePacsAdmitMessageFromAPatient() throws HL7Exception, ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("PATIENT_IDENTIFIER");

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Doe");
        patientName.setGivenName("John");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-27-1979");
        patient.setBirthdate(birthDate);
        patient.setGender("M");

        String xml = converter.convertToPacsFormat(patient, "A01");

        assertThat(xml, startsWith("MSH|^~\\&|||||||ADT^A01||P|2.3\r"));
        assertThat(xml, endsWith("PID|||PATIENT_IDENTIFIER||Doe^John||197908270000|M\r"));

    }

    @Test
    public void shouldGeneratePacsUpdateMessageFromAPatient() throws HL7Exception, ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("PATIENT_IDENTIFIER");

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Doe");
        patientName.setGivenName("John");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-27-1979");
        patient.setBirthdate(birthDate);
        patient.setGender("M");

        String xml = converter.convertToPacsFormat(patient, "A08");

        assertThat(xml, startsWith("MSH|^~\\&|||||||ADT^A08||P|2.3\r"));
        assertThat(xml, endsWith("PID|||PATIENT_IDENTIFIER||Doe^John||197908270000|M\r"));

    }
}
