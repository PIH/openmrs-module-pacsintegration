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
package org.openmrs.module.pacsintegration.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.pacsintegration.OutboundQueue;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.db.PacsIntegrationDAO;
import org.openmrs.module.pacsintegration.incoming.IncomingMessageListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of
 * {@link org.openmrs.module.pacsintegration.api.PacsIntegrationService}.
 */
public class PacsIntegrationServiceImpl extends BaseOpenmrsService implements PacsIntegrationService {
	
	protected final Log log = LogFactory.getLog(this.getClass());

	private PacsIntegrationDAO dao;

    private IncomingMessageListener hl7Listener;
	
	@Override
    @Transactional
	public void sendMessageToPacs(String message) {
		OutboundQueue outbound = new OutboundQueue(message);
		dao.saveOutboundQueue(outbound);
	}

    @Override
    public void initializeHL7Listener() {
        hl7Listener.initialize();
    }

    @Override
    public boolean isHL7ListenerRunning() {
        return hl7Listener.isRunning();
    }

    @Override
    public void stopHL7Listener() {
        hl7Listener.stop();
    }

    /**
     * @param dao the dao to set
     */
    public void setDao(PacsIntegrationDAO dao) {
        this.dao = dao;
    }

    /**
     * @return the dao
     */
    public PacsIntegrationDAO getDao() {
        return dao;
    }

    public IncomingMessageListener getHl7Listener() {
        return hl7Listener;
    }

    public void setHl7Listener(IncomingMessageListener hl7Listener) {
        this.hl7Listener = hl7Listener;
    }
}


