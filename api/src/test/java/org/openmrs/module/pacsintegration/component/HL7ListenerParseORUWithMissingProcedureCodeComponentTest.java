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

package org.openmrs.module.pacsintegration.component;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.test.jupiter.NonTransactionalBaseModuleContextSensitiveTest;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.radiologyapp.RadiologyProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class HL7ListenerParseORUWithMissingProcedureCodeComponentTest extends NonTransactionalBaseModuleContextSensitiveTest {

    /*
     After upgrading to run against 1.11.x I'm running into a problem here, either:
        1) I run the test transactionally and for some reason the HL7Listener doesn't have access to the test data (?)
        2) I run non-transactionally and the tests run into problems since there isn't a rollback after each one

     I didn't want to get too blocked on this, so I ended up doing something hacky and breaking each test up into it's own class,
     thereby forcing them to run independently
     */

    protected static final String XML_METADATA_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-metadata.xml";
    protected static final String XML_MAPPINGS_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-mappings.xml";
    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    private char header = '\u000B';
    private char trailer = '\u001C';

    @Autowired
    private PatientService patientService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private RadiologyProperties radiologyProperties;

    @Before
    public void setup() throws Exception {
        executeDataSet(XML_METADATA_DATASET);
        executeDataSet(XML_MAPPINGS_DATASET);
        executeDataSet(XML_DATASET);
        Context.getService(PacsIntegrationService.class).initializeHL7Listener();
    }

    @After
    public void tearDown() throws Exception {
        deleteAllData();
    }

    @Test
    public void shouldListenForAndParseORU_R01MessageMissingProcedureCode() throws Exception {

        List<Patient> patients = patientService.getPatients(null, "101-6", Collections.singletonList(emrApiProperties.getPrimaryIdentifierType()), true);
        assertThat(patients.size(), is(1));  // sanity check
        Patient patient = patients.get(0);
        List<Encounter> encounters = encounterService.getEncounters(patient, null, null, null, null, Collections.singletonList(radiologyProperties.getRadiologyReportEncounterType()),
                null, null, null, false);
        assertThat(encounters.size(), is(0));  // sanity check

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000001297||||20130228170556||||||||||||MBL^CR||||||F|||||||M123&Goodrich&Mark&&&&||||20130228170556\r" +
                "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r";

        Thread.sleep(2000);    // give the simple server time to start

        Socket socket = new Socket("127.0.0.1", 6665);

        PrintStream writer = new PrintStream(socket.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer.print(header);
        writer.print(message);
        writer.print(trailer +"\r");
        writer.flush();

        Thread.sleep(2000);

        // confirm that report encounter has been created and has obs (we more thoroughly test the handler in the ORU_R01 handler and Radiology Service (in emr module) tests)
        encounters = encounterService.getEncounters(patient, null, null, null, null, Collections.singletonList(radiologyProperties.getRadiologyReportEncounterType()),
                null, null, null, false);
        assertThat(encounters.size(), is(1));
        assertThat(encounters.get(0).getObs().size(), is(3));  // only 3 because no procedure obs

        // confirm that the proper ack is sent out
        String response = reader.readLine();
        assertThat(response, containsString("|ACK|"));

    }

}
