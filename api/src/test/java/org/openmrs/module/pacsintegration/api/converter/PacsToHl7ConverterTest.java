package org.openmrs.module.pacsintegration.api.converter;

import java.util.Date;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.PID;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.pacsintegration.InboundQueue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class PacsToHl7ConverterTest {

    private PacsToHl7Converter converter;

    @Before
    public void setup() {
        converter = new PacsToHl7Converter();
    }

    @Test
    public void shouldParseInboundMessageIntoHl7Object() throws HL7Exception {

        InboundQueue inboundQueue = new InboundQueue();
        inboundQueue.setDateCreated(new Date());
        inboundQueue.setProcessed(false);
        inboundQueue.setMessage("MSH|^~\\&|HMI||RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.2|\r" +
                "PID|1||GG2F98||Patient^Test^||19770222|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||1297|36554-4^CHEST|||20130228170556||||||||||||MBL^CR||||||F|||\r" +
                "OBR|1||1297|||||&Goodrich&Mark&&&&^||||20130228170556\r" );

        ORU_R01 oruR01 = converter.convertToORU_R01(inboundQueue);

        assertNotNull(oruR01);
        assertThat(oruR01.getVersion(), is("2.2") );

        MSH msh = oruR01.getMSH();
        assertThat(msh.getDateTimeOfMessage().getTimeOfAnEvent().getValue(), is("20130228174549"));

        ORU_R01_PATIENT patient = oruR01.getRESPONSE().getPATIENT();
        assertThat(patient.getPID().getPatientIDInternalID(0).getID().getValue(), is("GG2F98"));

        //ORU_R01_ORDER_OBSERVATION order = oruR01.getRESPONSE().getORDER_OBSERVATION();
        //assertThat();
    }

    // test various null/error cases
}
