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
package org.openmrs.module.pacsintegration.api.converter;

import com.thoughtworks.xstream.XStream;
import org.openmrs.Order;
import org.openmrs.module.pacsintegration.api.messages.OrmMessage;

public class OrderToPacsConverter {

    private XStream xstream = new XStream();

    public OrderToPacsConverter() {
        xstream.alias("OrmMessage", OrmMessage.class);
    }

    public String convertToPacsFormat(Order order, String orderControl) {
        OrmMessage ormMessage = new OrmMessage(order, orderControl);
        return xstream.toXML(ormMessage);
    }
}
