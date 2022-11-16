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
package org.openmrs.module.pacsintegration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.handler.HL7Handler;
import org.openmrs.module.pacsintegration.listener.PacsEventListener;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class PacsIntegrationActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	protected Log log = LogFactory.getLog(getClass());

	@Override
	public void setDaemonToken(DaemonToken daemonToken) {
		HL7Handler.setDaemonToken(daemonToken);
		PacsEventListener.setDaemonToken(daemonToken);
	}

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		log.info("PACS Integration Module started");
		// we are now starting this via the PIH Core module activator so that we can determine whether to start or not based on configuratio
		//Context.getService(PacsIntegrationService.class).initializeHL7Listener();
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping PACS Integration Module");
        Context.getService(PacsIntegrationService.class).stopHL7Listener();
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("PACS Integration Module stopped");
	}
	
}
