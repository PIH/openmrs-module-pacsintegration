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

import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(PacsIntegrationService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface PacsIntegrationService extends OpenmrsService {
	
	/**
	 * Adds a message to the outgoing queue for PACS
	 */
	public void sendMessageToPacs(String message);

    /**
     * Initializes the HL7 listener
     */
    public void initializeHL7Listener();

    /**
     * @return true if the hl7 listener is running
     */
    public boolean isHL7ListenerRunning();

    /**
     * Stops the HL7 listener
     */
    public void stopHL7Listener();
}

