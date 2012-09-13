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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Order;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;

import java.io.StringWriter;
import java.io.Writer;

public class ConversionUtils {

    /**
     * Given an order and an order control code, creates an ORMMessage object
     */
    public static ORMMessage createORMMessage(Order order, String orderControl) {

        if (order == null) {
            throw new PACSIntegrationException("Order cannot be null");
        }

        if (StringUtils.isEmpty(orderControl)) {
            // default to a new order
            orderControl = "NW";
        }

        ORMMessage ormMessage = new ORMMessage();

        ormMessage.setAccessionNumber(order.getAccessionNumber());
        ormMessage.setDateOfBirth(PACSIntegrationConstants.hl7DateFormat.format(order.getPatient().getBirthdate()));
        // TODO: ormMessage.setDeviceLocation();
        ormMessage.setFamilyName(order.getPatient().getFamilyName());
        ormMessage.setGivenName(order.getPatient().getGivenName());
        // TODO: ormMessage.setModality();
        ormMessage.setOrderControl(orderControl);
        // TODO: we should set this to specific identifier that is configured, not just the preferred one?
        ormMessage.setPatientId(order.getPatient().getPatientIdentifier().getIdentifier());
        ormMessage.setPatientSex(order.getPatient().getGender());
        // TODO: is the start date the order date?
        ormMessage.setScheduledExamDatetime(PACSIntegrationConstants.hl7DateFormat.format(order.getStartDate()));
        // TODO: ormMessage.setSendingFacility();
        // TODO: ormMessage.setUniversalServiceID();
        // TODO; ormMessage.setUniversalServiceIDText();

        return ormMessage;
    }

    /**
     * Performs basic serialization using Simple library
     */
    public static String serialize(Object obj) {

        if (obj == null) {
            throw new PACSIntegrationException("Item to serialize cannot be null");
        }

        Writer stringWriter = new StringWriter();
        Serializer serializer = new Persister();

        try {
            serializer.write(obj, stringWriter);
        }
        catch (Exception e) {
            throw new PACSIntegrationException("Unable to serialize " + obj, e);
        }

        return stringWriter.toString();

    }
}
