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

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.SimpleServer;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;

import java.util.Map;

public class HL7Listener {

    private HL7Service hl7Service;

    private PacsIntegrationProperties pacsIntegrationProperties;

    private Map<String, Application> handlers;

    public void initialize() {

        hl7Service = new SimpleServer(pacsIntegrationProperties.getHL7ListenerPort());

        for (Map.Entry<String,Application>  entry : handlers.entrySet()) {
            String messageType = entry.getKey().split("_")[0];
            String triggerEvent = entry.getKey().split("_").length > 1 ? entry.getKey().split("_") [1] : null;
            hl7Service.registerApplication(messageType, triggerEvent, entry.getValue());
        }

        hl7Service.start();

    }

    public void stop() {
        if (hl7Service != null) {
            hl7Service.stop();
        }
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

    public PacsIntegrationProperties getPacsIntegrationProperties() {
        return pacsIntegrationProperties;
    }

    public void setPacsIntegrationProperties(PacsIntegrationProperties pacsIntegrationProperties) {
        this.pacsIntegrationProperties = pacsIntegrationProperties;
    }

    public Map<String, Application> getHandlers() {
        return handlers;
    }

    public void setHandlers(Map<String, Application> handlers) {
        this.handlers = handlers;
    }
}
