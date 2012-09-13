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

import org.apache.commons.io.IOUtils;

import java.net.Socket;

public class TransmissionUtils {

    public static void sendMessage(Message message) {
        try {
            Socket socket = new Socket(PacsIntegrationGlobalProperties.GLOBAL_PROPERTY_MIRTH_IP_ADDRESS(), PacsIntegrationGlobalProperties.GLOBAL_PROPERTY_MIRTH_INPUT_PORT());
            IOUtils.write(ConversionUtils.serialize(message), socket.getOutputStream());
            socket.close();
        }
        catch (Exception e) {
            throw new PacsIntegrationException("Error connecting to socket" , e);
        }
    }

}
