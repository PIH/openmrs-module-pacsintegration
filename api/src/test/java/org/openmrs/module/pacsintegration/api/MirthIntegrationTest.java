package org.openmrs.module.pacsintegration.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.net.Socket;

public class MirthIntegrationTest extends BaseModuleContextSensitiveTest {

    protected final Log log = LogFactory.getLog(getClass());

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet(XML_DATASET);
    }


    @Test
    public void trySendingViaSocket() throws Exception {

        try {
            Socket socket = new Socket("127.0.0.1", 6661);
            IOUtils.write("<patient><givenName>Bob</givenName><familyName>Barker</familyName></patient>", socket.getOutputStream());
            socket.close();
        }
        catch (Exception e) {
            System.out.println("Error connecting to socket: " + e.getMessage());
        }


    }
}
