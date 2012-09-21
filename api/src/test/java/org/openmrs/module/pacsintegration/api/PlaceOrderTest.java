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

package org.openmrs.module.pacsintegration.api;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.event.advice.GeneralEventAdvice;
import org.openmrs.module.pacsintegration.ConversionUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.annotation.NotTransactional;

/**
 * This test fails because when our listener receives an order created message, that order doesn't seem to be in the DB.
 * I don't know if this is an artifact of the test framework (particularly the MVCC hack) or a real bug.
 */
public class PlaceOrderTest extends BaseModuleContextSensitiveTest {

    protected final Log log = LogFactory.getLog(getClass());

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    /**
     * See http://listarchives.openmrs.org/Limitations-of-H2-for-unit-tests-td7560958.html .
     * This avoids: org.h2.jdbc.JdbcSQLException: Timeout trying to lock table "ORDERS"; SQL statement:
     * 
     * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
     */
    @Override
    public Properties getRuntimeProperties() {
        Properties props = super.getRuntimeProperties();
        String url = props.getProperty(Environment.URL);
        if (url.contains("jdbc:h2:") && !url.contains(";MVCC=TRUE")) {
            props.setProperty(Environment.URL, url + ";MVCC=true");
        }
        return props;
    }
    
    @Before
    public void setupDatabase() throws Exception {
        executeDataSet(XML_DATASET);
    }

    @Test
    @NotTransactional
    public void testPlacingRadiologyOrderShouldTriggerOutgoingMessage() throws Exception {
    	// we want to mock PacsIntegrationService but not other services, so we cannot use powermock to mock Context
        PacsIntegrationService mockPacsIntegrationService = Mockito.mock(PacsIntegrationService.class);
        ServiceContext.getInstance().setService(PacsIntegrationService.class, mockPacsIntegrationService);
        
        // the @StartModule annotation leads to classloader issues, so we manually register this advice for the Event module
        // originally we had this class-level annotation: @StartModule({ "org/openmrs/module/pacsintegration/include/event-1.0.omod" })
        Context.addAdvice(OrderService.class, new GeneralEventAdvice());

        Order order = new Order();
        order.setOrderType(Context.getOrderService().getOrderType(1001));
        order.setPatient(Context.getPatientService().getPatient(7));
        order.setConcept(Context.getConceptService().getConcept(18));
        order.setStartDate(new Date());
        Context.getOrderService().saveOrder(order);

        // wait for a few seconds to give the test time to propogate
        Thread.sleep(5000);

        Mockito.verify(mockPacsIntegrationService).sendMessageToPacs(ConversionUtils.serialize(ConversionUtils.createORMMessage(order, "NW")));

        // TODO: since this is not transactional does it pollute our test database?
    }


}
