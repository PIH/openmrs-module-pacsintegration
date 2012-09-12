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

        ORMMessage ormMessage = ConversionUtils.createORMMessage(order, "SC");

        Assert.assertEquals("54321", ormMessage.getAccessionNumber());
        Assert.assertEquals("197608250000", ormMessage.getDateOfBirth());
        // TODO: Assert.assertEquals("", ormMessage.getDeviceLocation());
        Assert.assertEquals("Chebaskwony", ormMessage.getFamilyName());
        Assert.assertEquals("Collet", ormMessage.getGivenName());
        // TODO: Assert.assertEquals(, ormMessage.getModality());
        Assert.assertEquals("SC", ormMessage.getOrderControl());
        Assert.assertEquals("6TS-4", ormMessage.getPatientId());
        Assert.assertEquals("F", ormMessage.getPatientSex());
        Assert.assertEquals("200808080000", ormMessage.getScheduledExamDatetime());
        // TODO: Assert.assertEquals("", ormMessage.getSendingFacility());
        // TODO: Assert.assertEquals("", ormMessage.getUniversalServiceID());
        // TODO: Assert.assertEquals("", ormMessage.getUniversalServiceIDText());


    }


    // TODO: add test to  make sure that is defaults to "NW"?
    // TODO: add tests to make sure fails when required fields are missing


    @Test
    public void serialize_shouldSerializeORMMessage() {

        Order order = Context.getOrderService().getOrder(1001);
        ORMMessage ormMessage = ConversionUtils.createORMMessage(order, "SC");

        // TODO: these are to mock the fields we aren't current handling--these should eventually be removed
        ormMessage.setDeviceLocation("E");
        ormMessage.setSendingFacility("A");
        ormMessage.setUniversalServiceID("B");
        ormMessage.setUniversalServiceIDText("C");
        ormMessage.setModality("D");

        String serializedORMMessage = ConversionUtils.serialize(ormMessage);

        TestUtils.assertContains("<ORMMessage>(.*)</ORMMessage>", serializedORMMessage);
        TestUtils.assertContains("<accessionNumber>54321</accessionNumber>", serializedORMMessage);
        TestUtils.assertContains("<dateOfBirth>197608250000</dateOfBirth>", serializedORMMessage);
        TestUtils.assertContains("<deviceLocation>E</deviceLocation>", serializedORMMessage);
        TestUtils.assertContains("<familyName>Chebaskwony</familyName>", serializedORMMessage);
        TestUtils.assertContains("<givenName>Collet</givenName>", serializedORMMessage);
        TestUtils.assertContains("<modality>D</modality>", serializedORMMessage);
        TestUtils.assertContains("<orderControl>SC</orderControl>", serializedORMMessage);
        TestUtils.assertContains("<patientId>6TS-4</patientId>", serializedORMMessage);
        TestUtils.assertContains("<patientSex>F</patientSex>", serializedORMMessage);
        TestUtils.assertContains("<scheduledExamDatetime>200808080000</scheduledExamDatetime>", serializedORMMessage);
        TestUtils.assertContains("<sendingFacility>A</sendingFacility>", serializedORMMessage);
        TestUtils.assertContains("<universalServiceID>B</universalServiceID>", serializedORMMessage);
        TestUtils.assertContains("<universalServiceIDText>C</universalServiceIDText>", serializedORMMessage);

    }

}
