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

public class ORU_R01ComponentTest extends BaseModuleContextSensitiveTest {

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
    ORU_R01Handler oruR01Handler;

    @BeforeEach
    public void setup() throws Exception {
        executeDataSet(XML_METADATA_DATASET);
        executeDataSet(XML_MAPPINGS_DATASET);
        executeDataSet(XML_DATASET);
        this.getConnection().commit();
        this.updateSearchIndex();
        Context.clearSession();
        Context.getService(PacsIntegrationService.class).initializeHL7Listener();
        oruR01Handler.setTaskRunner(runnable -> runnable.run());
    }

    @Test
    public void shouldCreateTwoReportsIfBodyDifferent() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyReportEncounters(patient).size(), is(0));  // sanity check

        String message1 =
                "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170556||||||||||||MBL^CR||||||F|||||||Test&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r";

        String message2 =
                "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170556||||||||||||MBL^CR||||||F|||||||Test&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Another Clinical Indication: ||||||F\r";

        processMessage(message1);
        processMessage(message2);

        assertThat(getRadiologyReportEncounters(patient).size(), is(2));
    }

    @Test
    public void shouldNotCreateDuplicateReport() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyReportEncounters(patient).size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170556||||||||||||MBL^CR||||||F|||||||Test&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r";

        processMessage(message);
        processMessage(message);

        assertThat(getRadiologyReportEncounters(patient).size(), is(1));
        assertThat(getRadiologyReportEncounters(patient).get(0).getObs().size(), is(4));
    }

    @Test
    public void shouldListenForAndParseORU_R01Message() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyReportEncounters(patient).size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r";

        Message resultMessage = processMessage(message);

        // confirm that report encounter has been created and has obs (we more thoroughly test the handler in the ORU_R01 handler and Radiology Service (in emr module) tests)
        List<Encounter> encounters = getRadiologyReportEncounters(patient);
        assertThat(encounters.size(), is(1));
        assertThat(encounters.get(0).getObs().size(), is(4));

        // confirm that the proper ack is sent out
        assertThat(resultMessage.encode(), containsString("|ACK|"));
    }

    @Test
    public void shouldListenForAndParseORU_R01MessageMissingProcedureCode() throws Exception {
        Patient patient = assertSinglePatient("101-6");
        assertThat(getRadiologyReportEncounters(patient).size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297||||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r";

        Message resultMessage = processMessage(message);

        Thread.sleep(2000);

        // confirm that report encounter has been created and has obs (we more thoroughly test the handler in the ORU_R01 handler and Radiology Service (in emr module) tests)
        List<Encounter> encounters = getRadiologyReportEncounters(patient);
        assertThat(encounters.size(), is(1));
        assertThat(encounters.get(0).getObs().size(), is(3));  // only 3 because no procedure obs

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
        return oruR01Handler.processMessage(hl7Message);
    }

    private List<Encounter> getRadiologyReportEncounters(Patient patient) {
        EncounterSearchCriteriaBuilder escb = new EncounterSearchCriteriaBuilder();
        escb.setPatient(patient);
        escb.setEncounterTypes(Collections.singletonList(radiologyProperties.getRadiologyReportEncounterType()));
        escb.setIncludeVoided(false);
        return encounterService.getEncounters(escb.createEncounterSearchCriteria());
    }
}
