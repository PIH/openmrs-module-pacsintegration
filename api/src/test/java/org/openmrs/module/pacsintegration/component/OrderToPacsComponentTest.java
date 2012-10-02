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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.event.advice.GeneralEventAdvice;
import org.openmrs.module.pacsintegration.listener.OrderEventListener;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;

@RunWith(SpringJUnit4ClassRunner.class)
public class OrderToPacsIT extends BaseModuleContextSensitiveTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    @InjectMocks
    private OrderEventListener orderEventListener;
    @Mock
    private PacsIntegrationService pacsIntegrationService;

	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";
	
	/**
	 * See http://listarchives.openmrs.org/Limitations-of-H2-for-unit-tests-td7560958.html . This
	 * avoids: org.h2.jdbc.JdbcSQLException: Timeout trying to lock table "ORDERS"; SQL statement:
	 * 
	 * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
	 */
	
	@Before
	public void setupDatabase() throws Exception {
        MockitoAnnotations.initMocks(this);
		executeDataSet(XML_DATASET);
		Context.addAdvice(OrderService.class, new GeneralEventAdvice());
	}

	@Test
	@NotTransactional
	public void testPlacingRadiologyOrderShouldTriggerOutgoingMessage() throws Exception {
		Order order = new Order();
		order.setOrderType(Context.getOrderService().getOrderType(1001));
		order.setPatient(Context.getPatientService().getPatient(7));
		order.setConcept(Context.getConceptService().getConcept(18));
		order.setStartDate(new Date());

        orderService.saveOrder(order);
		
		Mockito.verify(pacsIntegrationService, timeout(5000)).sendMessageToPacs(any(String.class));
	}
	
}
