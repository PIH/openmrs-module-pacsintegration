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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.StartModule;

@Ignore
@StartModule({ "org/openmrs/module/pacsintegration/include/event-1.0.omod" })
public class PlaceOrderTest extends BaseModuleContextSensitiveTest {

    protected final Log log = LogFactory.getLog(getClass());

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet(XML_DATASET);
    }


    @Test
    public void testPlacingRadiologyOrderShouldTriggerOutgoingMessage() {

        PacsIntegrationService mockPacsIntegrationService = Mockito.mock(PacsIntegrationService.class);

        Order order = new Order();
        order.setOrderType(Context.getOrderService().getOrderType(1001));
        order.setPatient(Context.getPatientService().getPatient(7));
        order.setConcept(Context.getConceptService().getConcept(18));

        Context.getOrderService().saveOrder(order);

        Mockito.verify(mockPacsIntegrationService).sendMessageToPacs("TEST");
    }


}
