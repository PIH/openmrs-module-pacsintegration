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

package org.openmrs.module.pacsintegration.listener;

import org.openmrs.OpenmrsObject;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.SubscribableEventListener;
import org.openmrs.module.pacsintegration.ConversionUtils;
import org.openmrs.module.pacsintegration.OrmMessage;
import org.openmrs.module.pacsintegration.PacsIntegrationConstants;
import org.openmrs.module.pacsintegration.PacsIntegrationGlobalProperties;
import org.openmrs.module.pacsintegration.api.PacsIntegrationService;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;

public class OrderEventListener implements SubscribableEventListener {


    @Override
    public void onMessage(Message message)  {

        try {
            MapMessage mapMessage = (MapMessage) message;

            if (Event.Action.CREATED.equals(mapMessage.getString("action")) && Order.class.equals(mapMessage.getString("classname"))) {
                Order order = Context.getOrderService().getOrderByUuid(mapMessage.getString("uuid"));

                if (PacsIntegrationGlobalProperties.RADIOLOGY_ORDER_TYPE_UUID().equals(order.getOrderType().getUuid())) {
                    OrmMessage ormMessage = ConversionUtils.createORMMessage(order, "NW");
                    Context.getService(PacsIntegrationService.class).sendMessageToPacs(ConversionUtils.serialize(ormMessage));
                }

            }
        }
        catch (Exception e) {
            //TODO: do something better
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Class<? extends OpenmrsObject>> subscribeToObjects() {
        // admittedly a very strange way to use a convenience method, but java
        // compilation wouldn't occur without this extra line
        Object classes = Arrays.asList(Order.class);
        return (List<Class<? extends OpenmrsObject>>) classes;
    }

    @Override
    public List<String> subscribeToActions() {
        return Arrays.asList(Event.Action.CREATED.name(), Event.Action.UPDATED.name(), Event.Action.VOIDED.name(), Event.Action.PURGED.name(), Event.Action.UNVOIDED.name());
    }

}
