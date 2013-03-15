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
import org.openmrs.module.pacsintegration.InboundQueue;
import org.openmrs.module.pacsintegration.OutboundQueue;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.api.converter.PacsToHl7Converter;
import org.openmrs.module.pacsintegration.api.db.PacsIntegrationDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of
 * {@link org.openmrs.module.pacsintegration.api.PacsIntegrationService}.
 */
public class PacsIntegrationServiceImpl extends BaseOpenmrsService implements PacsIntegrationService {
	
	protected final Log log = LogFactory.getLog(this.getClass());

    private PacsToHl7Converter pacsToHl7Converter;

	private PacsIntegrationDAO dao;
	
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
	
	@Override
    @Transactional
	public void sendMessageToPacs(String message) {
		OutboundQueue outbound = new OutboundQueue(message);
		dao.saveOutboundQueue(outbound);
	}

    @Override
    @Transactional
    public void readNewMessagesFromPacs() {

        for (InboundQueue message : dao.getNewMessagesFromInboundQueue()) {

            // remember null checks
            // note that we are only handline ORU messages
            // convert, process, mark as read

        }

    }
}
