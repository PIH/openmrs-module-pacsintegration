package org.openmrs.module.pacsintegration.api;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class PatientToPacsConverterTest {

    private PatientToPacsConverter converter;

    @Before
    public void setup() {
        converter = new PatientToPacsConverter();
    }

    @Test
    public void shouldGeneratePacsMessageFromAPatient() throws ParseException {
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

        String xml = converter.convertToPacsFormat(patient, "");

        assertThat(xml, startsWith("<ADTMessage>"));
        assertThat(xml, containsString("<patientSex>M</patientSex>"));
        assertThat(xml, containsString("<dateOfBirth>197908270000</dateOfBirth>"));
        assertThat(xml, containsString("<givenName>John</givenName>"));
        assertThat(xml, containsString("<familyName>Doe</familyName>"));
        assertThat(xml, containsString("<patientIdentifier>PATIENT_IDENTIFIER</patientIdentifier>"));
        assertThat(xml, containsString("<sendingFacility></sendingFacility>"));
        assertThat(xml, endsWith("</ADTMessage>"));
    }
}
