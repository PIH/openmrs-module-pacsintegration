package org.openmrs.module.pacsintegration;

import org.apache.commons.io.IOUtils;

import java.net.Socket;

public class TransmissionUtils {

    public static void sendMessage(Message message) {
        try {
            Socket socket = new Socket(PACSIntegrationGlobalProperties.GLOBAL_PROPERTY_MIRTH_IP_ADDRESS(), PACSIntegrationGlobalProperties.GLOBAL_PROPERTY_MIRTH_INPUT_PORT());
            IOUtils.write(ConversionUtils.serialize(message), socket.getOutputStream());
            socket.close();
        }
        catch (Exception e) {
            throw new PACSIntegrationException("Error connecting to socket" , e);
        }
    }

}
