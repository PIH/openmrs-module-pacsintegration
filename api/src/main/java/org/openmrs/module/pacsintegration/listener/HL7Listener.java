package org.openmrs.module.pacsintegration.listener;

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

import java.util.Map;

public class HL7Listener {

    // TODO: make this a global property
    public static final Integer port = 6662;

    private HL7Service hl7Service;

    private Map<String, Application> handlers;

    public void initialize() {

        hl7Service = new SimpleServer(port, new MinLowerLayerProtocol(), new PipeParser());

        for (Map.Entry<String,Application>  entry : handlers.entrySet()) {
            String messageType = entry.getKey().split("_")[0];
            String triggerEvent = entry.getKey().split("_").length > 1 ? entry.getKey().split("_") [1] : null;
            hl7Service.registerApplication(messageType, triggerEvent, entry.getValue());
        }

        hl7Service.start();

    }

    public void stop() {
        hl7Service.stop();
    }

    /**
     * Getters and Setters
     */
    public HL7Service getHl7Service() {
        return hl7Service;
    }

    public void setHl7Service(HL7Service hl7Service) {
        this.hl7Service = hl7Service;
    }

    public Map<String, Application> getHandlers() {
        return handlers;
    }

    public void setHandlers(Map<String, Application> handlers) {
        this.handlers = handlers;
    }
}
