package org.openmrs.module.pacsintegration.handler;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

/**
 * Concrete implementations of this class should include the logic to execute on a particular incoming message to
 * produce a particular result message.  The incoming message, result message, and any exception during processing
 * should be stored in the provided instance variables for inspection by consumers
 */
public abstract class HL7Task implements Runnable {

    private Message incomingMessage;
    private Message resultMessage;
    private HL7Exception hl7Exception;

    public Message getIncomingMessage() {
        return incomingMessage;
    }

    public void setIncomingMessage(Message incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    public Message getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(Message resultMessage) {
        this.resultMessage = resultMessage;
    }

    public HL7Exception getHl7Exception() {
        return hl7Exception;
    }

    public void setHl7Exception(HL7Exception hl7Exception) {
        this.hl7Exception = hl7Exception;
    }
}
