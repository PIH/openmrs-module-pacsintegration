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

package org.openmrs.module.pacsintegration.util;

import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.model.v23.segment.MSH;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HL7Utils {

    public static DateFormat getHl7DateFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }

    public static MSH populateMessageHeader(MSH msh, Date dateTime, String messageType, String triggerEvent, String sendingFacility) throws DataTypeException {

        msh.getFieldSeparator().setValue("|");
        msh.getEncodingCharacters().setValue("^~\\&");
        msh.getSendingFacility().getNamespaceID().setValue(sendingFacility);
        msh.getDateTimeOfMessage().getTimeOfAnEvent().setValue(getHl7DateFormat().format(dateTime));
        msh.getMessageType().getMessageType().setValue(messageType);
        msh.getMessageType().getTriggerEvent().setValue(triggerEvent);
        //  TODO: do we need to send Message Control ID?
        msh.getProcessingID().getProcessingID().setValue("P");  // stands for production (?)
        msh.getVersionID().setValue("2.3");

        return msh;
    }

    public static ACK generateACK(String messageControlId, String sendingFacility) throws DataTypeException {

        ACK ack = new ACK();

        populateMessageHeader(ack.getMSH(), new Date(), "ACK", null, sendingFacility);

        ack.getMSA().getAcknowledgementCode().setValue("AA");
        ack.getMSA().getMessageControlID().setValue(messageControlId);

        return ack;
    }

    public static ACK generateErrorACK(String messageControlId, String sendingFacility, String errorMessage) throws DataTypeException {

        ACK ack = new ACK();

        populateMessageHeader(ack.getMSH(), new Date(), "ACK", null, sendingFacility);

        ack.getMSA().getAcknowledgementCode().setValue("AR");
        ack.getMSA().getMessageControlID().setValue(messageControlId);
        ack.getMSA().getTextMessage().setValue(errorMessage);

        return ack;
    }

}
