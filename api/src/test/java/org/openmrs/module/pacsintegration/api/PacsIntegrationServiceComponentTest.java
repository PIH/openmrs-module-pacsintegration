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
package org.openmrs.module.pacsintegration.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests {@link PacsIntegrationService}}.
 */
public class PacsIntegrationServiceComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    PacsIntegrationService pacsIntegrationService;

	@Qualifier("adminService")
	@Autowired
	AdministrationService administrationService;

	protected static final String XML_METADATA_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-metadata.xml";
	protected static final String XML_MAPPINGS_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset-mappings.xml";
	protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

	@BeforeEach
	public void setupDatabase() throws Exception {
		executeDataSet(XML_METADATA_DATASET);
		executeDataSet(XML_MAPPINGS_DATASET);
		executeDataSet(XML_DATASET);
		this.getConnection().commit();
		this.updateSearchIndex();
		Context.clearSession();
	}
	
	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(PacsIntegrationService.class));
	}
	
	@Test
	public void testSendingOrderMessageShouldWriteEntryToOutboundQueue() {
		// send the message
        String message = "TEST MESSAGE";
        pacsIntegrationService.sendMessageToPacs(message);
		
		// confirm that it has been stored in the outbound queue
		List<List<Object>> results = administrationService.executeSQL(
				"SELECT message FROM pacsintegration_outbound_queue ORDER BY date_created DESC", true
		);
		String messageInQueue = (String) results.get(0).get(0);
        assertThat(message, is(messageInQueue));
	}
}
