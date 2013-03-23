package org.openmrs.module.pacsintegration.listener;

import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;
import org.openmrs.module.pacsintegration.application.ORM_R01Application;

public class HL7Listener {

    // TODO: make this a global property
    public static final Integer port = 6662;

    private HL7Service hl7Service;

    public HL7Listener() {

        hl7Service = new SimpleServer(port, new MinLowerLayerProtocol(), new PipeParser());
        hl7Service.registerApplication("ORU","R01", new ORM_R01Application());
        hl7Service.start();

    }

}
