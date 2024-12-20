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

package org.openmrs.module.pacsintegration.outgoing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.runner.TaskRunner;
import org.openmrs.module.pacsintegration.test.TransactionalTestService;
import org.openmrs.module.radiologyapp.RadiologyOrder;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.MapMessage;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;

public class OrderToPacsComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionalTestService transactionalTestService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private OrderEventListener orderEventListener;

    @Autowired
    private OrderToPacsConverter converter;

    private PacsIntegrationService pacsIntegrationService;

    private TestTaskRunner testTaskRunner;

    protected static final String XML_METADATA_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-metadata.xml";
    protected static final String XML_MAPPINGS_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-mappings.xml";
    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

	@BeforeEach
	public void setupDatabase() throws Exception {
        testTaskRunner = new TestTaskRunner();
        pacsIntegrationService = mock(PacsIntegrationService.class);
        orderEventListener.setPacsIntegrationService(pacsIntegrationService);
        orderEventListener.setTaskRunner(testTaskRunner);
        Event event = new Event();
        event.setSubscription(orderEventListener);

        executeDataSet(XML_METADATA_DATASET);
        executeDataSet(XML_MAPPINGS_DATASET);
        executeDataSet(XML_DATASET);
        this.getConnection().commit();
        this.updateSearchIndex();
        Context.clearSession();
    }

    @Test
    public void testSavingOrderWithEncounterShouldTriggerOutgoingMessage() throws Exception {
        RadiologyOrder order = new RadiologyOrder();
        order.setOrderType(Context.getOrderService().getOrderType(1001));
        order.setPatient(Context.getPatientService().getPatient(7));
        order.setConcept(Context.getConceptService().getConcept(18));
        order.setCareSetting(Context.getOrderService().getCareSetting(1));
        order.setOrderer(Context.getProviderService().getProvider(1));

        Encounter encounter = new Encounter();
        encounter.setPatient(Context.getPatientService().getPatient(7));
        encounter.setEncounterDatetime(new Date());
        encounter.addOrder(order);
        encounter.setEncounterType(Context.getEncounterService().getEncounterType(1003));
        transactionalTestService.saveEncounter(encounter);

        Thread.sleep(2000);

        OutgoingMessageTask task = testTaskRunner.getTask();
        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.getIncomingMessage());
        Assertions.assertInstanceOf(MapMessage.class, task.getIncomingMessage());
        MapMessage mapMessage = (MapMessage) task.getIncomingMessage();
        assertThat(mapMessage.getString("action"), equalTo(Event.Action.CREATED.toString()));
        assertThat(mapMessage.getString("uuid"), equalTo(order.getUuid()));
    }

    @Test
    public void testSendingTestOrderShouldNotTriggerOutgoingMessage() throws Exception {

        TestOrder order = new TestOrder();
        order.setOrderType(Context.getOrderService().getOrderType(2));
        order.setPatient(Context.getPatientService().getPatient(7));
        order.setConcept(Context.getConceptService().getConcept(18));
        order.setCareSetting(Context.getOrderService().getCareSetting(1));
        order.setOrderer(Context.getProviderService().getProvider(1));

        Encounter encounter = new Encounter();
        encounter.setPatient(Context.getPatientService().getPatient(7));
        encounter.setEncounterDatetime(new Date());
        encounter.addOrder(order);
        encounter.setEncounterType(Context.getEncounterService().getEncounterType(1003));
        transactionalTestService.saveEncounter(encounter);

        Thread.sleep(2000);

        OutgoingMessageTask task = testTaskRunner.getTask();
        Assertions.assertNull(task);
    }

    @Test
    public void testPlacingRadiologyOrderShouldGenerateProperMessage() throws Exception {
        Context.getAdministrationService().setGlobalProperty(PacsIntegrationConstants.GP_DEFAULT_LOCALE, "en_GB");
        RadiologyOrder order = new RadiologyOrder();
        order.setOrderType(Context.getOrderService().getOrderType(1001));
        order.setPatient(Context.getPatientService().getPatient(7));
        order.setConcept(Context.getConceptService().getConcept(18));
        order.setDateActivated(new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012"));
        order.setExamLocation(locationService.getLocation(1));
        order.setCareSetting(Context.getOrderService().getCareSetting(1));
        order.setOrderer(Context.getProviderService().getProvider(1));

        Encounter encounter = new Encounter();
        encounter.setPatient(Context.getPatientService().getPatient(7));
        encounter.setEncounterDatetime(new SimpleDateFormat("MM-dd-yyyy").parse("08-08-2012"));
        encounter.addOrder(order);
        encounter.setEncounterType(Context.getEncounterService().getEncounterType(1003));
        encounter.addProvider(encounterService.getEncounterRole(1003), providerService.getProvider(1));
        transactionalTestService.saveEncounter(encounter);

        Thread.sleep(2000);

        OutgoingMessageTask task = testTaskRunner.getTask();
        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.getIncomingMessage());
        Assertions.assertInstanceOf(MapMessage.class, task.getIncomingMessage());
        MapMessage mapMessage = (MapMessage) task.getIncomingMessage();
        assertThat(mapMessage.getString("action"), equalTo(Event.Action.CREATED.toString()));
        assertThat(mapMessage.getString("uuid"), equalTo(order.getUuid()));

        Order orderFromUuid = orderService.getOrderByUuid(mapMessage.getString("uuid"));
        Assertions.assertNotNull(orderFromUuid);
        String hl7Message = converter.convertToPacsFormat(order, "NW");
        assertThat(hl7Message, startsWith("MSH|^~\\&||Mirebalais|||"));
        // TODO: test that a valid date is passed
        assertThat(hl7Message, containsString("||ORM^O01||P|2.3\r"));
        assertThat(hl7Message, containsString("PID|||6TS-4||Chebaskwony^Collet||19760825000000|F\r"));
        assertThat(hl7Message, containsString("PV1|||1FED2^^^^^^^^Unknown Location|||||Test^User^Super\r"));
        assertThat(hl7Message, containsString("ORC|NW\r"));
        // we need to break out the last two lines because the order number (ORD-1) varies based on when test is executed
        // due to the fact that the tests are non-transactional and therefore don't roll back
        assertThat(hl7Message, containsString("OBR|||ORD-"));
        assertThat(hl7Message, endsWith("|127689^FOOD ASSISTANCE||||||||||||||||||||||||||||||||20120808000000\r"));
    }

    /**
     * This test task running bypasses all the actual logic in the given HL7Task, and just confirms that the correct
     * task recieved the correct message
     */
    static class TestTaskRunner implements TaskRunner {

        private OutgoingMessageTask task;

        @Override
        public void run(Runnable runnable) {
            if (runnable instanceof OutgoingMessageTask) {
                task = (OutgoingMessageTask) runnable;
            }
        }

        public OutgoingMessageTask getTask() {
            return task;
        }
    }
}
