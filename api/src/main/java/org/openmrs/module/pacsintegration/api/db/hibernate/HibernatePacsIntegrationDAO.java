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
package org.openmrs.module.pacsintegration.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.pacsintegration.InboundQueue;
import org.openmrs.module.pacsintegration.OutboundQueue;
import org.openmrs.module.pacsintegration.api.db.PacsIntegrationDAO;

/**
 * It is a default implementation of
 * {@link org.openmrs.module.pacsintegration.api.db.PacsIntegrationDAO}.
 */
public class HibernatePacsIntegrationDAO implements PacsIntegrationDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

    @Override
    public void saveInboundQueue(InboundQueue inboundQueue) {
        sessionFactory.getCurrentSession().saveOrUpdate(inboundQueue);
    }

    @Override
	public void saveOutboundQueue(OutboundQueue outboundQueue) {
		sessionFactory.getCurrentSession().saveOrUpdate(outboundQueue);
	}

    @Override
    public List<InboundQueue> getNewMessagesFromInboundQueue() {

        // should order by date
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
