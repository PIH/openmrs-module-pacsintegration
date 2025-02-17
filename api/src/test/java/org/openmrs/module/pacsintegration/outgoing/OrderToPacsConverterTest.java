/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.pacsintegration.outgoing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
import org.openmrs.module.pacsintegration.outgoing.OrderToPacsConverter;
import org.openmrs.module.radiologyapp.RadiologyOrder;
import org.openmrs.module.radiologyapp.RadiologyProperties;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderToPacsConverterTest {

    private OrderToPacsConverter converter;

    private PatientIdentifierType patientIdentifierType = new PatientIdentifierType();

    private Concept testXrayConcept;

    private Concept testXrayConceptSet;

    private Concept testCTConcept;

    private Concept testCTConceptSet;

    private Concept testUSConcept;

    private Concept testUSConceptSet;

    private EncounterRole clinicialEncounterRole;

    private Location examLocation;

    // TODO: test some error cases

    @BeforeEach
    public void setup() {
        ConceptMapType sameAsConceptMapType = new ConceptMapType();
        sameAsConceptMapType.setName("SAME-AS");
        sameAsConceptMapType.setUuid(UUID.randomUUID().toString());

        ConceptSource procedureCodeConceptSource = new ConceptSource();
        procedureCodeConceptSource.setUuid(UUID.randomUUID().toString());

        LocationAttributeType locationCodeAttributeType = new LocationAttributeType();
        locationCodeAttributeType.setUuid(UUID.randomUUID().toString());

        ConceptName testXrayConceptName = new ConceptName();
        testXrayConceptName.setName("Left-hand x-ray");
        testXrayConceptName.setLocale(new Locale("en"));

        ConceptReferenceTerm testXrayConceptReferenceTerm = new ConceptReferenceTerm();
        testXrayConceptReferenceTerm.setCode("123ABC");
        testXrayConceptReferenceTerm.setConceptSource(procedureCodeConceptSource);

        ConceptMap sameAsConceptXrayMap = new ConceptMap();
        sameAsConceptXrayMap.setConceptMapType(sameAsConceptMapType);
        sameAsConceptXrayMap.setConceptReferenceTerm(testXrayConceptReferenceTerm);

        testXrayConcept = new Concept();
        testXrayConcept.addName(testXrayConceptName);
        testXrayConcept.addConceptMapping(sameAsConceptXrayMap);

        testXrayConceptSet = new Concept();
        testXrayConceptSet.addSetMember(testXrayConcept);

        ConceptName testCTConceptName = new ConceptName();
        testCTConceptName.setName("Head without contrast");
        testCTConceptName.setLocale(new Locale("en"));

        ConceptReferenceTerm testCTConceptReferenceTerm = new ConceptReferenceTerm();
        testCTConceptReferenceTerm.setCode("456DEF");
        testCTConceptReferenceTerm.setConceptSource(procedureCodeConceptSource);

        ConceptMap sameAsConceptCTMap = new ConceptMap();
        sameAsConceptCTMap.setConceptMapType(sameAsConceptMapType);
        sameAsConceptCTMap.setConceptReferenceTerm(testCTConceptReferenceTerm);

        testCTConcept = new Concept();
        testCTConcept.addName(testCTConceptName);
        testCTConcept.addConceptMapping(sameAsConceptCTMap);

        testCTConceptSet = new Concept();
        testCTConceptSet.addSetMember(testCTConcept);

        ConceptName testUSConceptName = new ConceptName();
        testUSConceptName.setName("blah");
        testUSConceptName.setLocale(new Locale("en"));

        ConceptReferenceTerm testUSConceptReferenceTerm = new ConceptReferenceTerm();
        testUSConceptReferenceTerm.setCode("789GHI");
        testUSConceptReferenceTerm.setConceptSource(procedureCodeConceptSource);

        ConceptMap sameAsConceptUSMap = new ConceptMap();
        sameAsConceptUSMap.setConceptMapType(sameAsConceptMapType);
        sameAsConceptUSMap.setConceptReferenceTerm(testUSConceptReferenceTerm);

        testUSConcept = new Concept();
        testUSConcept.addName(testUSConceptName);
        testUSConcept.addConceptMapping(sameAsConceptUSMap);

        testUSConceptSet = new Concept();
        testUSConceptSet.addSetMember(testUSConcept);

        clinicialEncounterRole = new EncounterRole();

        examLocation = new Location();
        examLocation.setId(2);
        examLocation.setName("Radiology");

        LocationAttribute locationCode = new LocationAttribute();
        locationCode.setValue("ABCDEF");
        locationCode.setAttributeType(locationCodeAttributeType);
        examLocation.setAttribute(locationCode);

        PatientService patientService = mock(PatientService.class);
        AdministrationService administrationService = mock(AdministrationService.class);
        ConceptService conceptService = mock(ConceptService.class);
        LocationService locationService = mock(LocationService.class);
        RadiologyProperties radiologyProperties = mock(RadiologyProperties.class);
        EmrApiProperties emrProperties = mock(EmrApiProperties.class);
        PacsIntegrationProperties pacsIntegrationProperties = mock(PacsIntegrationProperties.class);

        when(patientService.getPatientIdentifierTypeByUuid(anyString())).thenReturn(patientIdentifierType);

        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY)).thenReturn("openmrs_mirebalais");
        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_PROCEDURE_CODE_CONCEPT_SOURCE_UUID)).thenReturn(procedureCodeConceptSource.getUuid());
        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_LOCATION_CODE_ATTRIBUTE_TYPE_UUID)).thenReturn(locationCodeAttributeType.getUuid());
        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_PATIENT_IDENTIFIER_TYPE_UUID)).thenReturn(patientIdentifierType.getUuid());

        when(conceptService.getConceptMapTypeByUuid(PacsIntegrationConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID)).thenReturn(sameAsConceptMapType);
        when(pacsIntegrationProperties.getProcedureCodesConceptSource()).thenReturn(procedureCodeConceptSource);

        when(locationService.getLocationAttributeTypeByUuid(locationCodeAttributeType.getUuid())).thenReturn(locationCodeAttributeType);

        when(emrProperties.getOrderingProviderEncounterRole()).thenReturn(clinicialEncounterRole);
        when(radiologyProperties.getXrayOrderablesConcept()).thenReturn(testXrayConceptSet);
        when(radiologyProperties.getCTScanOrderablesConcept()).thenReturn(testCTConceptSet);
        when(radiologyProperties.getUltrasoundOrderablesConcept()).thenReturn(testUSConceptSet);

        converter = new OrderToPacsConverter();
        converter.setPatientService(patientService);
        converter.setAdminService(administrationService);
        converter.setConceptService(conceptService);
        converter.setLocationService(locationService);
        converter.setRadiologyProperties(radiologyProperties);
        converter.setEmrApiProperties(emrProperties);
        converter.setPacsIntegrationProperties(pacsIntegrationProperties);
    }

    @Test
    public void shouldGenerateMessageFromAnXRayOrder() throws Exception {

        RadiologyOrder order = mock(RadiologyOrder.class);
        UUID uuid = UUID.randomUUID();
        Patient patient = createPatient();
        Encounter encounter = createEncounterWithProvider();

        when(order.getOrderNumber()).thenReturn(uuid.toString());
        when(order.getDateActivated()).thenReturn((new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012")));
        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(testXrayConcept);
        when(order.getUrgency()).thenReturn(Order.Urgency.STAT);
        when(order.getClinicalHistory()).thenReturn("Patient fell off horse\r\nAnd broke back");
        when(order.getExamLocation()).thenReturn(examLocation);
        when(order.getEncounter()).thenReturn(encounter);

        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||19760825000000|F\r"));
        assertThat(hl7Message, containsString("PV1|||ABCDEF^^^^^^^^Radiology|||||123^Joseph^Wayne"));
        assertThat(hl7Message, containsString("ORC|SC\r"));
        assertThat(hl7Message, endsWith("OBR|||" + uuid.toString() + "|123ABC^Left-hand x-ray|||||||||||||||CR||||||||^^^^^STAT||||^Patient fell off horse~^And broke back|||||20120808000000\r"));
    }

    @Test
    public void shouldGenerateMessageFromACTOrder() throws Exception {

        RadiologyOrder order = mock(RadiologyOrder.class);
        UUID uuid = UUID.randomUUID();
        Patient patient = createPatient();
        Encounter encounter = createEncounterWithProvider();

        when(order.getOrderNumber()).thenReturn(uuid.toString());
        when(order.getDateActivated()).thenReturn((new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012")));
        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(testCTConcept);
        when(order.getUrgency()).thenReturn(Order.Urgency.STAT);
        when(order.getClinicalHistory()).thenReturn("Patient fell off horse\r\nAnd broke back");
        when(order.getExamLocation()).thenReturn(examLocation);
        when(order.getEncounter()).thenReturn(encounter);


        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||19760825000000|F\r"));
        assertThat(hl7Message, containsString("PV1|||ABCDEF^^^^^^^^Radiology|||||123^Joseph^Wayne"));
        assertThat(hl7Message, containsString("ORC|SC\r"));
        assertThat(hl7Message, endsWith("OBR|||" + uuid.toString() + "|456DEF^Head without contrast|||||||||||||||CT||||||||^^^^^STAT||||^Patient fell off horse~^And broke back|||||20120808000000\r"));
    }

    @Test
    public void shouldGenerateMessageFromAUSOrder() throws Exception {

        RadiologyOrder order = mock(RadiologyOrder.class);
        UUID uuid = UUID.randomUUID();
        Patient patient = createPatient();
        Encounter encounter = createEncounterWithProvider();

        when(order.getOrderNumber()).thenReturn(uuid.toString());
        when(order.getDateActivated()).thenReturn((new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012")));
        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(testUSConcept);
        when(order.getUrgency()).thenReturn(Order.Urgency.STAT);
        when(order.getClinicalHistory()).thenReturn("Patient fell off horse\r\nAnd broke back");
        when(order.getExamLocation()).thenReturn(examLocation);
        when(order.getEncounter()).thenReturn(encounter);

        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||19760825000000|F\r"));
        assertThat(hl7Message, containsString("PV1|||ABCDEF^^^^^^^^Radiology|||||123^Joseph^Wayne"));
        assertThat(hl7Message, containsString("ORC|SC\r"));
        assertThat(hl7Message, endsWith("OBR|||" + uuid.toString() + "|789GHI^blah|||||||||||||||US||||||||^^^^^STAT||||^Patient fell off horse~^And broke back|||||20120808000000\r"));
    }


    @Test
    public void shouldGenerateMessageForAnonymousPatient() throws Exception {
        RadiologyOrder order = mock(RadiologyOrder.class);
        UUID uuid = UUID.randomUUID();
        Patient patient = createAnonymousPatient();
        Encounter encounter = createEncounter();

        when(order.getOrderNumber()).thenReturn(uuid.toString());
        when(order.getDateActivated()).thenReturn((new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012")));
        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(testXrayConcept);
        when(order.getUrgency()).thenReturn(Order.Urgency.ROUTINE);
        when(order.getClinicalHistory()).thenReturn("Patient fell off horse");
        when(order.getExamLocation()).thenReturn(examLocation);
        when(order.getEncounter()).thenReturn(encounter);

        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||UNKNOWN^UNKNOWN|||F\r"));
        assertThat(hl7Message, containsString("ORC|SC\r"));
        assertThat(hl7Message, endsWith("OBR|||" + uuid.toString() + "|123ABC^Left-hand x-ray|||||||||||||||CR||||||||||||^Patient fell off horse|||||20120808000000\r"));
    }

    @Test
    public void shouldTruncateLogPatientName() throws Exception {

        RadiologyOrder order = mock(RadiologyOrder.class);
        UUID uuid = UUID.randomUUID();
        Patient patient = createPatientWithLongName();
        Encounter encounter = createEncounterWithProvider();

        when(order.getOrderNumber()).thenReturn(uuid.toString());
        when(order.getDateActivated()).thenReturn((new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012")));
        when(order.getPatient()).thenReturn(patient);
        when(order.getConcept()).thenReturn(testXrayConcept);
        when(order.getUrgency()).thenReturn(Order.Urgency.STAT);
        when(order.getClinicalHistory()).thenReturn("Patient fell off horse\r\nAnd broke back");
        when(order.getExamLocation()).thenReturn(examLocation);
        when(order.getEncounter()).thenReturn(encounter);

        String hl7Message = converter.convertToPacsFormat(order, "SC");

        assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Super Duper Long Name Crazy Lo^Even My Given Name Is Longer T||19760825000000|F\r"));
        assertThat(hl7Message, containsString("PV1|||ABCDEF^^^^^^^^Radiology|||||123^Joseph^Wayne"));
        assertThat(hl7Message, containsString("ORC|SC\r"));
        assertThat(hl7Message, endsWith("OBR|||" + uuid.toString() + "|123ABC^Left-hand x-ray|||||||||||||||CR||||||||^^^^^STAT||||^Patient fell off horse~^And broke back|||||20120808000000\r"));
    }


    private Patient createPatient() throws ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("6TS-4");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Chebaskwony");
        patientName.setGivenName("Collet");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-25-1976");
        patient.setBirthdate(birthDate);
        patient.setGender("F");
        return patient;
    }

    private Patient createPatientWithLongName() throws ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("6TS-4");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("Super Duper Long Name Crazy Long Name");
        patientName.setGivenName("Even My Given Name Is Longer Than You Would Believe");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);
        Date birthDate = new SimpleDateFormat("MM-dd-yyyy").parse("08-25-1976");
        patient.setBirthdate(birthDate);
        patient.setGender("F");
        return patient;
    }

    private Patient createAnonymousPatient() throws ParseException {
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier("6TS-4");
        patientIdentifier.setIdentifierType(patientIdentifierType);

        PersonName patientName = new PersonName();
        patientName.setFamilyName("UNKNOWN");
        patientName.setGivenName("UNKNOWN");

        Patient patient = new Patient();
        patient.addIdentifier(patientIdentifier);
        patient.addName(patientName);

        patient.setGender("F");
        return patient;
    }

    private Provider createProvider() {
        Provider provider = new Provider();

        Person person = new Person();
        PersonName providerName = new PersonName();
        providerName.setFamilyName("Joseph");
        providerName.setGivenName("Wayne");
        person.addName(providerName);

        provider.setPerson(person);
        provider.setIdentifier("123");
        return provider;
    }

    private Encounter createEncounter() {
        return new Encounter();
    }

    private Encounter createEncounterWithProvider() {
        Encounter encounter = createEncounter();
        encounter.setEncounterProviders(new HashSet<>());
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setEncounter(encounter);
        encounterProvider.setEncounterRole(clinicialEncounterRole);
        encounterProvider.setProvider(createProvider());
        encounter.getEncounterProviders().add(encounterProvider);
        return encounter;
    }
}
