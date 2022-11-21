/*
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

package org.openmrs.module.pacsintegration.incoming;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.radiologyapp.RadiologyProperties;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

public class ORM_O01ComponentTest extends BaseModuleContextSensitiveTest {

    protected static final String XML_METADATA_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-metadata.xml";
    protected static final String XML_MAPPINGS_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-mappings.xml";
    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Autowired
    private PatientService patientService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private RadiologyProperties radiologyProperties;

    @Autowired
    ORM_O01Handler ormO01Handler;

    @BeforeEach
    public void setup() throws Exception {
        executeDataSet(XML_METADATA_DATASET);
        executeDataSet(XML_MAPPINGS_DATASET);
        executeDataSet(XML_DATASET);
        this.getConnection().commit();
        this.updateSearchIndex();
        Context.clearSession();
        Context.getService(PacsIntegrationService.class).initializeHL7Listener();
        ormO01Handler.setTaskRunner(runnable -> runnable.run());
    }

    @Test
    public void shouldNotImportORM_001MessageWithDuplicateOrderNumber() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyStudyEncounters(patient).size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        processMessage(message);
        processMessage(message);

        // confirm that only one encounter has been created
        assertThat(getRadiologyStudyEncounters(patient).size(), is(1));
    }

    @Test
    public void shouldListenForAndParseORM_001Message() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyStudyEncounters(patient).size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|1435^Duck^Donald\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20111009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20111009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";

        Message resultMessage = processMessage(message);

        List<Encounter> encounters = getRadiologyStudyEncounters(patient);
        assertThat(encounters.size(), is(1));
        assertThat(encounters.get(0).getObs().size(), is(3));

        // confirm that the proper ack is sent out
        assertThat(resultMessage.encode(), containsString("|ACK|"));
    }

    private Patient assertSinglePatient(String identifier) {
        List<Patient> patients = patientService.getPatients(null, identifier, Collections.singletonList(emrApiProperties.getPrimaryIdentifierType()), true);
        assertThat(patients.size(), is(1));  // sanity check
        return patients.get(0);
    }

    private Message processMessage(String message) throws Exception {
        PipeParser pipeParser = new PipeParser();
        Message hl7Message = pipeParser.parse(message);
        return ormO01Handler.processMessage(hl7Message);
    }

    private List<Encounter> getRadiologyStudyEncounters(Patient patient) {
        EncounterSearchCriteriaBuilder escb = new EncounterSearchCriteriaBuilder();
        escb.setPatient(patient);
        escb.setEncounterTypes(Collections.singletonList(radiologyProperties.getRadiologyStudyEncounterType()));
        escb.setIncludeVoided(false);
        return encounterService.getEncounters(escb.createEncounterSearchCriteria());
    }
}
