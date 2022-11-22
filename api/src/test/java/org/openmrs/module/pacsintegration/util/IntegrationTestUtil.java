package org.openmrs.module.pacsintegration.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintStream;
import java.net.Socket;

/**
 * These are *not* actual tests (hence why they are ignored),but utility methods that can be run to test
 * sending HL7 messages to HUM-CI as part of manual testing; patient identifier (Y4K2YL), order accession number (0000234252),
 * order id and type (36554-4^X-ray of chest, 1 view) and other data points can be modified as necessary
 */
public class IntegrationTestUtil {

    private char header = '\u000B';
    private char trailer = '\u001C';

    @Test
    @Ignore
    public void sendTestORMMesssage() throws Exception {

        String message = "MSH|^~\\&|HMI||RAD|REPORTS|20130228174643||ORM^O01|RTS01CE16057B105AC0|P|2.3|\r" +
                "PID|1||Y4K2YL||Dylan^Bob^||19950101000000|M||||||||||\r" +
                "ORC|\r" +
                "OBR|1||0000234252|36554-4^X-ray of chest, 1 view|||20220828170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|RP|||||||||F\r" +
                "OBX|2|TX|EventType^EventType|1|REVIEWED\r" +
                "OBX|3|CN|Technologist^Technologist|1|MAADH^Goodrich^Mark\r" +
                "OBX|4|TX|ExamRoom^ExamRoom|1|100AcreWoods\r" +
                "OBX|5|TS|StartDateTime^StartDateTime|1|20221009215317\r" +
                "OBX|6|TS|StopDateTime^StopDateTime|1|20221009215817\r" +
                "OBX|7|TX|ImagesAvailable^ImagesAvailable|1|1\r" +
                "ZDS|2.16.840.1.113883.3.234.1.3.101.1.2.1013.2011.15607503.2^HMI^Application^DICOM\r";


        Socket socket = new Socket("humci.pih-emr.org", 6663);

        PrintStream writer = new PrintStream(socket.getOutputStream());
        writer.print(header);
        writer.print(message);
        writer.print(trailer +"\r");
        writer.flush();

    }

    @Test
    @Ignore
    public void sendTestORUMessage() throws Exception {

        String message = "MSH|^~\\&|HMI|Mirebalais Hospital|RAD|REPORTS|20130228174549||ORU^R01|RTS01CE16055AAF5290|P|2.3|\r" +
                "PID|1||Y4K2YL||Dylan^Bob^||19950101000000|M||||||||||\r" +
                "PV1|1||||||||||||||||||\r" +
                "OBR|1||0000234252|36554-4^X-ray of chest, 1 view|||20220828170350||||||||||||MBL^CR||||||P|||||||&Goodrich&Mark&&&&^||||20130228170350\r" +
                "OBX|1|TX|36554-4^X-ray of chest, 1 view||Hello world again from new daemon method! ||||||F\r";

        Socket socket = new Socket("humci.pih-emr.org", 6663);

        PrintStream writer = new PrintStream(socket.getOutputStream());
        writer.print(header);
        writer.print(message);
        writer.print(trailer +"\r");
        writer.flush();

    }

}
