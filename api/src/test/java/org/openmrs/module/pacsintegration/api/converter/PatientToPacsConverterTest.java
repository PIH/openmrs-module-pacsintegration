package org.openmrs.module.pacsintegration.api.converter;

import ca.uhn.hl7v2.HL7Exception;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PatientToPacsConverterTest {

    private PatientToPacsConverter converter;

    private PatientIdentifierType patientIdentifierType = new PatientIdentifierType();

    @Before
    public void setup() {
        PatientService patientService = mock(PatientService.class);
        AdministrationService administrationService = mock(AdministrationService.class);
        when(patientService.getPatientIdentifierTypeByUuid(anyString())).thenReturn(patientIdentifierType);

        converter = new PatientToPacsConverter(patientService, administrationService);
    }

    @Test
    public void shouldGeneratePacsAdmitMessageFromAPatient() throws HL7Exception, ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("PATIENT_IDENTIFIER");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Doe");
        patientName.setGivenName("John");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-27-1979");
        patient.setBirthdate(birthDate);
        patient.setGender("M");

        String hl7Message = converter.convertToAdmitMessage(patient);

        assertThat(hl7Message, startsWith("MSH|^~\\&|||||||ADT^A01||P|2.3\r"));
        assertThat(hl7Message, endsWith("PID|||PATIENT_IDENTIFIER||Doe^John||197908270000|M\r"));

    }

    @Test
    public void shouldGeneratePacsUpdateMessageFromAPatient() throws HL7Exception, ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("PATIENT_IDENTIFIER");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Doe");
        patientName.setGivenName("John");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-27-1979");
        patient.setBirthdate(birthDate);
        patient.setGender("M");

        String hl7Message = converter.convertToUpdateMessage(patient);

        assertThat(hl7Message, startsWith("MSH|^~\\&|||||||ADT^A08||P|2.3\r"));
        assertThat(hl7Message, endsWith("PID|||PATIENT_IDENTIFIER||Doe^John||197908270000|M\r"));

    }
}
