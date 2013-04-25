package org.openmrs.module.pacsintegration.component;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.converter.PatientToPacsConverter;
import org.openmrs.module.pacsintegration.listener.PatientEventListener;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore     // currently ignoring this test since we aren't supporting sending ADT messages at this point
public class PatientToPacsComponentTest extends BaseModuleContextSensitiveTest {

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Autowired
    private PatientService patientService;
    @Autowired
    @InjectMocks
    private PatientEventListener patientEventListener;
    @Mock
    private PacsIntegrationService pacsIntegrationService;
    @Mock
    private PatientToPacsConverter pacsConverter;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        executeDataSet(XML_DATASET);
    }


    @Test
    @NotTransactional
    public void sendsCreatedPatientsToPacs() throws Exception {
        Patient patient = createPatient();
        String hl7Message = "ADT_01";
        when(pacsConverter.convertToAdmitMessage(patient)).thenReturn(hl7Message);

        patientService.savePatient(patient);

        verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(hl7Message);
    }

    @Test
    @NotTransactional
    public void sendsUpdatedPatientsToPacs() throws Exception {
        Patient patient = patientService.getPatient(2);
        patient.setBirthdate(new Date());

        String hl7Message = "ADT_08";
        when(pacsConverter.convertToUpdateMessage(patient)).thenReturn(hl7Message);

        patientService.savePatient(patient);

        verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(hl7Message);
    }


    private Patient createPatient() throws ParseException {
        PatientIdentifierType identifierType = new PatientIdentifierType(1);
        Location location = new Location(1);

        PatientIdentifier patientIdentifier = new PatientIdentifier("PATIENT_IDENTIFIER", identifierType, location);
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

        return patient;
    }
}
