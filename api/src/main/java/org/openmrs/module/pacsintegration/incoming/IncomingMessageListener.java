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

package org.openmrs.module.pacsintegration.incoming;

import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.SimpleServer;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.pacsintegration.PacsIntegrationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("hl7listener")
public class IncomingMessageListener {

    private HL7Service hl7Service;

    @Setter
    @Autowired
    private PacsIntegrationProperties pacsIntegrationProperties;

    @Setter @Getter
    @Autowired
    List<IncomingMessageHandler> handlers;

    public void initialize() {
        hl7Service = new SimpleServer(pacsIntegrationProperties.getHL7ListenerPort());
        if (handlers != null) {
            for (IncomingMessageHandler handler : handlers) {
                hl7Service.registerApplication(handler.getMessageType(), handler.getTriggerEvent(), handler);
            }
        }
        hl7Service.start();
    }

    public boolean isRunning() {
        return hl7Service != null && hl7Service.isRunning();
    }

    public void stop() {
        if (hl7Service != null) {
            hl7Service.stop();
        }
    }
}
