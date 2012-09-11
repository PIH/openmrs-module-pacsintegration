package org.openmrs.module.pacsintegration.api;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.pacsintegration.ConversionUtils;
import org.openmrs.module.pacsintegration.ORMMessage;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class ConversionUtilsTest extends BaseModuleContextSensitiveTest {

    protected final Log log = LogFactory.getLog(getClass());

    protected static final String XML_DATASET = "org/openmrs/module/pacsintegration/include/pacsIntegrationTestDataset.xml";

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet(XML_DATASET);
    }

    @Test
    public void createORMMessage_shouldCreateORMMessageFromOrder() {

        Order order = Context.getOrderService().getOrder(1001);

        ORMMessage orgMessage = ConversionUtils.createORMMessage(order, "SC");

        Assert.assertEquals("54321", orgMessage.getAccessionNumber());
        Assert.assertEquals("197608250000", orgMessage.getDateOfBirth());
        // TODO: Assert.assertEquals("", orgMessage.getDeviceLocation());
        Assert.assertEquals("Chebaskwony", orgMessage.getFamilyName());
        Assert.assertEquals("Collet", orgMessage.getGivenName());
        // TODO: Assert.assertEquals(, orgMessage.getModality());
        Assert.assertEquals("SC", orgMessage.getOrderControl());
        Assert.assertEquals("6TS-4", orgMessage.getPatientId());
        Assert.assertEquals("F", orgMessage.getPatientSex());
        Assert.assertEquals("200808080000", orgMessage.getScheduledExamDatetime());
        // TODO: Assert.assertEquals("", orgMessage.getSendingFacility());
        // TODO: Assert.assertEquals("", orgMessage.getUniversalServiceID());
        // TODO: Assert.assertEquals("", orgMessage.getUniversalServiceIDText());


    }


    // TODO: add test to  make sure that is defaults to "NW"?
    // TODO: add tests to make sure fails when required fields are missing
}
