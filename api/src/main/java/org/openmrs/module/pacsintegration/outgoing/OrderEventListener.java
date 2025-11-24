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

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;
import org.openmrs.module.pacsintegration.runner.TaskRunner;
import org.openmrs.module.radiologyapp.RadiologyOrder;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;
import javax.jms.Message;

import static org.openmrs.event.Event.Action.CREATED;
import static org.openmrs.event.Event.Action.PURGED;
import static org.openmrs.event.Event.Action.UNVOIDED;
import static org.openmrs.event.Event.Action.UPDATED;
import static org.openmrs.event.Event.Action.VOIDED;

@Component
public class OrderEventListener implements EventListener {

	protected final Log log = LogFactory.getLog(getClass());

	public static Event.Action[] ACTIONS = {CREATED, UPDATED, VOIDED, UNVOIDED, PURGED};

    private OrderService orderService;

    private PacsIntegrationService pacsIntegrationService;

    private OrderToPacsConverter converter;

	protected TaskRunner taskRunner;

	private boolean subscribed = false;

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

	public void setup() {
		StopWatch sw = new StopWatch();
		log.warn("Subscribing to Radiology Order events");
		sw.start();
		for (Event.Action action : ACTIONS) {
			Event.subscribe(RadiologyOrder.class, action.name(), this);
			sw.split();
			log.warn("Subscribed to " + action + " in: " +  sw.toSplitString());
		}
		sw.stop();
		log.warn("Subscribed to Radiology Order events in " +  sw);
		subscribed = true;
	}

	public void teardown() {
		if (subscribed) {
			log.warn("Unsubscribing to Radiology Order events");
			for (Event.Action action : ACTIONS) {
				Event.unsubscribe(RadiologyOrder.class, action, this);
			}
		}
		subscribed = false;
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
