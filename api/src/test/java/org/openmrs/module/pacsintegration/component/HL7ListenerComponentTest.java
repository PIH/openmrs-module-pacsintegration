package org.openmrs.module.pacsintegration.component;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.pacsintegration.PacsIntegrationActivator;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class HL7ListenerComponentTest extends BaseModuleContextSensitiveTest {

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Before
    public void setup() throws Exception {
        executeDataSet(XML_DATASET);
    }

    @Ignore
    @Test
    public void stall() throws InterruptedException {
        new PacsIntegrationActivator().started();
        Thread.sleep(60000);
    }


}
