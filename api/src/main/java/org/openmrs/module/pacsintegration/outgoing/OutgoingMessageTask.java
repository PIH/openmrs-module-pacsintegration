package org.openmrs.module.pacsintegration.outgoing;

import javax.jms.Message;

/**
 * Concrete implementations of this class should include the logic to execute on a particular Event message to
 * produce a particular output message.  The incoming message should be stored in the provided instance variables
 * for inspection by consumers
 */
public abstract class OutgoingMessageTask implements Runnable {

    private Message incomingMessage;

    public OutgoingMessageTask(Message incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    public Message getIncomingMessage() {
        return incomingMessage;
    }
}
