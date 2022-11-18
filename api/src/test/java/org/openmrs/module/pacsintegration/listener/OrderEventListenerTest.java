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

package org.openmrs.module.pacsintegration.listener;

import ca.uhn.hl7v2.app.Application;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.handler.ORM_O01Handler;
import org.openmrs.module.pacsintegration.handler.ORU_R01Handler;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The goal of this class is to test that the hl7 server is successfully running as expected and that messages are
 * delivered to it successfully and routed to the appropriate handler.  It does not test that the handlers
 * handle the messages successfully - that testing is done in the tests for the handlers themselves.
 */
public class OrderEventListenerTest extends BaseModuleContextSensitiveTest {

    protected static final String XML_METADATA_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-metadata.xml";
    protected static final String XML_MAPPINGS_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-mappings.xml";
    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Autowired
    HL7Listener hl7Listener;

    @Autowired
    ORM_O01Handler orm_o01Handler;

    @Autowired
    ORU_R01Handler oru_r01Handler;

    private AdministrationService administrationService;

    private static final char HEADER_CHAR = '\u000B';
    private static final char TRAILER_CHAR = '\u001C';

    private static final String TEST_ORU_R01_MESSAGE =
            HEADER_CHAR +
            "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
            "PID|1||101-6||Patient^Test^||19770222|M||||||||||\r" +
            "PV1|1||||||||||||||||||\r" +
            "OBR|1||0000001297|127689^SOME_X-RAY|||20130228170556||||||||||||MBL^CR||||||F|||||||Test&Goodrich&Mark&&&&||||20130228170556\r" +
            "OBX|1|TX|127689^SOME_X-RAY||Clinical Indication: ||||||F\r"
            + TRAILER_CHAR + "\r";

    private static final String TEST_ORM_O01_MESSAGE =
            HEADER_CHAR +
            "MSH|^~\\&|HMI||RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
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
            "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r"
            + TRAILER_CHAR + "\r";

    @BeforeEach
    public void setup() throws Exception {
        executeDataSet(XML_METADATA_DATASET);
        executeDataSet(XML_MAPPINGS_DATASET);
        executeDataSet(XML_DATASET);
        this.getConnection().commit();
        this.updateSearchIndex();
        Context.clearSession();
        if (!hl7Listener.isRunning()) {
            Context.getService(PacsIntegrationService.class).initializeHL7Listener();
        }
        Thread.sleep(2000);
    }

    @Test
    public void shouldStartAndStopHl7Service() throws Exception  {
        Assertions.assertTrue(hl7Listener.isRunning());
        hl7Listener.stop();
        Thread.sleep(2000);
        Assertions.assertFalse(hl7Listener.isRunning());
    }

    @Test
    public void shouldHaveHandlersForKnownMessages() {
        Assertions.assertEquals(2, hl7Listener.getHandlers().size());
        Assertions.assertTrue(hl7Listener.getHandlers().containsKey("ORM_O01"));
        Assertions.assertTrue(hl7Listener.getHandlers().containsKey("ORU_R01"));
    }

    @Test
    public void shouldHaveRegisteredORM_O01Hander() {
        Application handler = hl7Listener.getHandlers().get("ORM_O01");
        Assertions.assertNotNull(handler);
        Assertions.assertEquals(handler.getClass(), ORM_O01Handler.class);
    }

    @Test
    public void shouldHaveRegisteredORU_R01Handler() {
        Application handler = hl7Listener.getHandlers().get("ORU_R01");
        Assertions.assertNotNull(handler);
        Assertions.assertEquals(handler.getClass(), ORU_R01Handler.class);
    }

    /**
     * This test is expected to log an exception, as the receiving Hl7Handler is not properly set up with an authenticated context
     * We are only testing that the message is routed to the appropriate handler by confirming that a particular method
     * is called in that handler's execution
     */
    @Test
    public void shouldReceiveORU_RO1MessageAndRouteToHandler() throws Exception  {
        administrationService = mock(AdministrationService.class);
        oru_r01Handler.setAdminService(administrationService);
        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY)).thenReturn("Mirebalais");
        try (Socket socket = new Socket("127.0.0.1", 6665)) {
            IOUtils.write(TEST_ORU_R01_MESSAGE, socket.getOutputStream(), "UTF-8");
        }
        Thread.sleep(2000);
        verify(administrationService).getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY);
    }

    /**
     * This test is expected to log an exception, as the receiving Hl7Handler is not properly set up with an authenticated context
     * We are only testing that the message is routed to the appropriate handler by confirming that a particular method
     * is called in that handler's execution
     */
    @Test
    public void shouldReceiveORM_OO1MessageAndRouteToHandler() throws Exception  {
        administrationService = mock(AdministrationService.class);
        orm_o01Handler.setAdminService(administrationService);
        when(administrationService.getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY)).thenReturn("Mirebalais");
        try (Socket socket = new Socket("127.0.0.1", 6665)) {
            IOUtils.write(TEST_ORM_O01_MESSAGE, socket.getOutputStream(), "UTF-8");
        }
        Thread.sleep(2000);
        verify(administrationService).getGlobalProperty(PacsIntegrationConstants.GP_SENDING_FACILITY);
    }
}
