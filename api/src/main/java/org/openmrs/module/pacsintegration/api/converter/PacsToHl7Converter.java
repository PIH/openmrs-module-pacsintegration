package org.openmrs.module.pacsintegration.api.converter;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.pacsintegration.InboundQueue;
import org.openmrs.module.pacsintegration.PacsIntegrationException;

public class PacsToHl7Converter {

    protected Log log = LogFactory.getLog(getClass());

    private Parser parser = new PipeParser();

    public ORU_R01 convertToORU_R01(InboundQueue inboundQueue) {

        ORU_R01 oruR01;

        try {
            Message message = parser.parse(inboundQueue.getMessage());

            if (!(message instanceof ORU_R01)) {
                throw new PacsIntegrationException("Unsupported message type received from inbound queue: "
                        + message.getClass());
            }

            oruR01 = (ORU_R01) message;
        }
        catch (EncodingNotSupportedException e) {
            log.error("Failed to parse incoming PACS message " + inboundQueue.getInboundQueueId()
                + " - " + inboundQueue.getMessage(), e);
            return null;   // note that we do a soft fail here
        }
        catch (HL7Exception e) {
            log.error("Failed to parse incoming PACS message " + inboundQueue.getInboundQueueId()
                    + " - " + inboundQueue.getMessage(), e);
            return null;   // note that we do a soft fail here
        }

        return oruR01;
    }
}
