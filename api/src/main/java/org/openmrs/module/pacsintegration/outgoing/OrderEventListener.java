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

import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.annotation.Handler;
import org.openmrs.api.OrderService;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.runner.TaskRunner;
import org.openmrs.module.radiologyapp.RadiologyOrder;

import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;

@Handler
public class OrderEventListener implements SubscribableEventListener {

    private OrderService orderService;

    private PacsIntegrationService pacsIntegrationService;

    private OrderToPacsConverter converter;

	protected TaskRunner taskRunner;

	public void setTaskRunner(TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}

    @Override
	public void onMessage(Message message) {
		taskRunner.run(new OutgoingMessageTask(message) {
			@Override
			public void run() {
				try {
					MapMessage mapMessage = (MapMessage) message;
					String action = mapMessage.getString("action");

					if (Event.Action.CREATED.toString().equals(action)) {
						String uuid = mapMessage.getString("uuid");

						Order order = orderService.getOrderByUuid(uuid);
						if (order == null) {
							throw new RuntimeException("Could not find the order this event tells us about! uuid=" + uuid);
						}

						String pacsMessage = converter.convertToPacsFormat((RadiologyOrder) order, "NW");
						pacsIntegrationService.sendMessageToPacs(pacsMessage);
					}
				} catch (Exception e) {
					//TODO: do something better
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Override
	public List<Class<? extends OpenmrsObject>> subscribeToObjects() {
		// admittedly a very strange way to use a convenience method, but java
		// compilation wouldn't occur without this extra line
		Object classes = Arrays.asList(RadiologyOrder.class);
		return (List<Class<? extends OpenmrsObject>>) classes;
	}
	
	@Override
	public List<String> subscribeToActions() {
		return Arrays.asList(Event.Action.CREATED.name(), Event.Action.UPDATED.name(), Event.Action.VOIDED.name(),
		    Event.Action.PURGED.name(), Event.Action.UNVOIDED.name());
	}

    public void setConverter(OrderToPacsConverter converter) {
        this.converter = converter;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setPacsIntegrationService(PacsIntegrationService pacsIntegrationService) {
        this.pacsIntegrationService = pacsIntegrationService;
    }
}
