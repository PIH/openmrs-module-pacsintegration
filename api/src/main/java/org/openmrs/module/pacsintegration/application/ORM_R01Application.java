package org.openmrs.module.pacsintegration.application;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ORM_R01Application implements Application {

    protected final Log log = LogFactory.getLog(this.getClass());

    @Override
    public Message processMessage(Message message) throws ApplicationException, HL7Exception {
        ORU_R01 oruR01 = (ORU_R01) message;
        return generateACK(oruR01.getMSH().getMessageControlID().getValue());
    }

    @Override
    public boolean canProcess(Message message) {
        return true;
    }

    // TODO: pull this out into utility methods

    private Message generateACK(String messageControlId) throws DataTypeException {
        ACK ack = new ACK();
        ack.getMSA().getAcknowledgementCode().setValue("AA");
        ack.getMSA().getMessageControlID().setValue(messageControlId);
        return ack;
    }
}
