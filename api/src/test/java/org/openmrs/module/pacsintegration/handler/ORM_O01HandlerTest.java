package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
import org.openmrs.module.radiologyapp.RadiologyOrder;
import org.openmrs.module.radiologyapp.RadiologyService;
import org.openmrs.module.radiologyapp.RadiologyStudy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class ORM_O01HandlerTest  {

    private ORM_O01Handler handler;

    private AdministrationService adminService;

    private PatientService patientService;

    private ConceptService conceptService;

    private RadiologyService radiologyService;

    private ProviderService providerService;

    private LocationService locationService;

    private EmrApiProperties emrApiProperties;

    private PacsIntegrationProperties pacsIntegrationProperties;

    private PatientIdentifierType primaryIdentifierType = new PatientIdentifierType();

    private Location mirebalaisHospital = new Location();

    private Patient patient;

    @Before
    public void setup() {
        adminService = mock(AdministrationService.class);
        when(adminService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY)).thenReturn("openmrs_mirebalais");

        patientService = mock(PatientService.class);
        radiologyService = mock(RadiologyService.class);
        conceptService = mock(ConceptService.class);
        providerService = mock(ProviderService.class);

        emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getPrimaryIdentifierType()).thenReturn(primaryIdentifierType);

        pacsIntegrationProperties = mock(PacsIntegrationProperties.class);
        ConceptSource loinc = new ConceptSource();
        loinc.setName("LOINC");
        when(pacsIntegrationProperties.getProcedureCodesConceptSource()).thenReturn(loinc);

        locationService = mock(LocationService.class);
        when(locationService.getLocation("Mirebalais Hospital")).thenReturn(mirebalaisHospital);

        User authenticatedUser = new User();
        mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);

        handler = new ORM_O01Handler();
        handler.setAdminService(adminService);
        handler.setPatientService(patientService);
        handler.setConceptService(conceptService);
        handler.setRadiologyService(radiologyService);
        handler.setProviderService(providerService);
        handler.setLocationService(locationService);
        handler.setEmrApiProperties(emrApiProperties);
        handler.setPacsIntegrationProperties(pacsIntegrationProperties);

        // sample patient for tests
        patient = new Patient(1);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(primaryIdentifierType);
        identifier.setIdentifier("GG2F98");
        patient.addIdentifier(identifier);

    }

    @Test
    public void shouldReturnACKButNotSaveRadiologyStudyIfNotReviewedOrReportedEventType() throws HL7Exception, ApplicationException {

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|SomeOtherEventType\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
        verify(radiologyService, never()).saveRadiologyStudy(any(RadiologyStudy.class));

    }

    @Test
    public void shouldReturnACKButNotSaveRadiologyStudyIfStudyAlreadyExistsWithThatOrderNumberType() throws HL7Exception, ApplicationException {

        // note that we are actually returning a study here, not null
        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(new RadiologyStudy());

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
        verify(radiologyService, never()).saveRadiologyStudy(any(RadiologyStudy.class));

    }

    @Test
    public void shouldReturnErrorACKIfNoPatientIdentifierInResponse() throws HL7Exception, ApplicationException {

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
        assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. No patient identifier specified."));
    }

    @Test
    public void shouldReturnErrorACKIfNoPatientWithIdentifier() throws HL7Exception, ApplicationException {

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);

        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(new ArrayList<Patient>());

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
        assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. No patient with identifier GG2F98"));
    }

    @Test
    public void shouldReturnErrorACKIfPatientIdentifierAndOrderNumberDontMatchSamePatient() throws HL7Exception, ApplicationException {

        Patient anotherPatient = new Patient(2);
        Concept procedure = new Concept();

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(anotherPatient);

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));

        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);


        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
        assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. Patient referenced in message different from patient attached to existing order."));
    }

    @Test
    public void shouldReturnErrorACKIfNoStudyDate() throws HL7Exception, ApplicationException {;

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
        assertThat(ack.getMSA().getTextMessage().getValue(), is("Cannot import message. Date performed must be specified."));

    }

    @Test
    public void shouldSaveRadiologyStudyForReviewedEvent() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setProcedure(procedure);
        expectedStudy.setAssociatedRadiologyOrder(radiologyOrder);
        expectedStudy.setTechnician(radiologyTechnician);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }

    @Test
    public void shouldSaveRadiologyStudyForReportedEvent() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REPORTED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setProcedure(procedure);
        expectedStudy.setAssociatedRadiologyOrder(radiologyOrder);
        expectedStudy.setTechnician(radiologyTechnician);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }

    @Test
    public void shouldSaveRadiologyStudyForNeedsOverreadEvent() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|NEEDSOVERREAD\r" +
                "OBX|3|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "OBX|4|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|5|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|6|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|7|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setProcedure(procedure);
        expectedStudy.setAssociatedRadiologyOrder(radiologyOrder);
        expectedStudy.setTechnician(radiologyTechnician);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }

    @Test
    public void shouldSaveRadiologyEvenIfAnotherPatientHasIdenticalIdentifierOfDifferentType() throws HL7Exception, ApplicationException {

        Patient anotherPatient = new Patient(2);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(new PatientIdentifierType());
        identifier.setIdentifier("GG2F98");
        anotherPatient.addIdentifier(identifier);

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Arrays.asList(patient, anotherPatient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setProcedure(procedure);
        expectedStudy.setAssociatedRadiologyOrder(radiologyOrder);
        expectedStudy.setTechnician(radiologyTechnician);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }

    @Test
    public void shouldSaveRadiologyEventIfProcedureNotSpecified() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297||||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setAssociatedRadiologyOrder(radiologyOrder);
        expectedStudy.setTechnician(radiologyTechnician);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }


    @Test
    public void shouldSaveRadiologyStudyEvenIfTechnicianOrImagesAvailableOrOrderIsNull() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(null);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));

        RadiologyStudy expectedStudy = new RadiologyStudy();
        expectedStudy.setPatient(patient);
        expectedStudy.setProcedure(procedure);
        expectedStudy.setAssociatedRadiologyOrder(null);
        expectedStudy.setTechnician(null);
        expectedStudy.setOrderNumber("0000001297");
        expectedStudy.setStudyLocation(mirebalaisHospital);
        expectedStudy.setImagesAvailable(null);

        Calendar cal = Calendar.getInstance();
        cal.set(2011,9,9);
        cal.set(Calendar.HOUR_OF_DAY, 21);
        cal.set(Calendar.MINUTE, 53);
        cal.set(Calendar.SECOND, 17);
        cal.set(Calendar.MILLISECOND, 00);
        expectedStudy.setDatePerformed(cal.getTime());

        verify(radiologyService).saveRadiologyStudy(argThat(new IsExpectedRadiologyStudy(expectedStudy)));
    }

    @Test
    public void  shouldNotFailIfDatetimeInFutureByLessThanFifteenMinutes() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        // create a report time that is 14 minutes in the future
        DateTime date = new DateTime();
        DateTime futureTime = date.plusMinutes(14);
        String futureTimeString = DateTimeFormat.forPattern("yyyyMMddHHmmss").print(futureTime);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||" + futureTimeString + "||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REPORTED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|" + futureTimeString + "\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AA"));
        verify(radiologyService).saveRadiologyStudy(argThat(new HasDatePerformedBetween(date.toDate(), new Date())));
    }


    // TODO change back to 15 minutes after we find a better fix for UHM-2434
    @Test
    public void shouldReturnErrorACKIfDateMoreThanSeventyFiveMinutesInFuture() throws HL7Exception, ApplicationException {

        RadiologyOrder radiologyOrder = new RadiologyOrder();
        radiologyOrder.setPatient(patient);
        Concept procedure = new Concept();
        Provider radiologyTechnician = new Provider();

        when(radiologyService.getRadiologyStudyByOrderNumber("0000001297")).thenReturn(null);
        when(patientService.getPatients(null, "GG2F98", Collections.singletonList(primaryIdentifierType), true))
                .thenReturn(Collections.singletonList(patient));
        when(radiologyService.getRadiologyOrderByOrderNumber("0000001297")).thenReturn(radiologyOrder);
        when(conceptService.getConceptByMapping("36554-4", "LOINC")).thenReturn(procedure);
        when(providerService.getProviderByIdentifier("1435")).thenReturn(radiologyTechnician);

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|36554-4^CHEST|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|DummyNotEventType||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REPORTED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|30001009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|30001009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        ACK ack = (ACK) handler.processMessage(parseMessage(message));

        assertThat(ack.getMSA().getAcknowledgementCode().getValue(), is("AR"));
        assertThat(ack.getMSA().getTextMessage().getValue(), is("Date cannot be more than 75 minutes in the future."));
    }

    private Message parseMessage(String message) throws HL7Exception {
        Parser parser = new PipeParser();
        return parser.parse(message);
    }

    public class IsExpectedRadiologyStudy implements ArgumentMatcher<RadiologyStudy> {

        private RadiologyStudy expectedStudy;

        public IsExpectedRadiologyStudy(RadiologyStudy study) {
            this.expectedStudy = study;
        }

        @Override
        public boolean matches(RadiologyStudy study) {
            assertThat(study.getOrderNumber(), is(expectedStudy.getOrderNumber()));
            assertThat(study.getAssociatedRadiologyOrder(), is(expectedStudy.getAssociatedRadiologyOrder()));
            assertThat(study.getProcedure(), is(expectedStudy.getProcedure()));
            assertThat(study.getDatePerformed(), is(expectedStudy.getDatePerformed()));
            assertThat(study.getPatient(), is(expectedStudy.getPatient()));
            assertThat(study.getTechnician(), is(expectedStudy.getTechnician()));
            assertThat(study.isImagesAvailable(), is(expectedStudy.isImagesAvailable()));
            assertThat(study.getStudyLocation(), is(expectedStudy.getStudyLocation()));

            return true;
        }

    }

    public class HasDatePerformedBetween implements ArgumentMatcher<RadiologyStudy> {

        private Date lowerRange;

        private Date upperRange;

        public HasDatePerformedBetween(Date lowerRange, Date upperRange) {
            this.lowerRange = lowerRange;
            this.upperRange = upperRange;
        }

        @Override
        public boolean matches(RadiologyStudy study) {
            assertThat(study.getDatePerformed(), greaterThanOrEqualTo(lowerRange));
            assertThat(study.getDatePerformed(), lessThanOrEqualTo(upperRange));

            return true;
        }

    }



}
