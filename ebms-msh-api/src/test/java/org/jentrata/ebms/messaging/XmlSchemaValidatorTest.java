package org.jentrata.ebms.messaging;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;

import java.io.InputStream;

/**
 * Unit test for org.jentrata.ebms.messaging.XmlSchemaValidator
 *
 * @author aaronwalker
 */
public class XmlSchemaValidatorTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockValid")
    protected MockEndpoint mockValid;

    @EndpointInject(uri = "mock:mockInvalid")
    protected MockEndpoint mockInvalid;

    @Test
    public void testValidEbmsUserMessage() throws Exception {
        mockInvalid.setExpectedMessageCount(0);
        mockValid.setExpectedMessageCount(1);
        mockValid.expectedHeaderReceived(XmlSchemaValidator.SCHEMA_VALID,true);

        Exchange request = createExchangeWithBody(EbmsUtils.toStringFromClasspath("sample-ebms-user-message.xml"));
        context().createProducerTemplate().send("direct:testValidator",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInvalidEbmsUserMessage() throws Exception {
        mockInvalid.setExpectedMessageCount(1);
        mockValid.setExpectedMessageCount(0);
        mockInvalid.expectedHeaderReceived(XmlSchemaValidator.SCHEMA_VALID,false);
        mockInvalid.expectedHeaderReceived(XmlSchemaValidator.SCHEMA_ERRORS,
                "cvc-datatype-valid.1.2.1: 'asss' is not a valid value for 'dateTime'.\n" +
                "cvc-type.3.1.3: The value 'asss' of element 'eb:Timestamp' is not valid.\n" +
                "cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type 'non-empty-string'.\n" +
                "cvc-complex-type.2.2: Element 'eb:PartyId' must have no element [children], and the value must be valid.\n");

        Exchange request = createExchangeWithBody(EbmsUtils.toStringFromClasspath("sample-invalid-ebms-user-message.xml"));
        context().createProducerTemplate().send("direct:testValidator",request);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        final XmlSchemaValidator xmlSchemaValidator = new XmlSchemaValidator(EbmsUtils.fileFromClasspath("schemas/ebms-header-3_0-200704.xsd"));
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:testValidator")
                    .doTry()
                        .setBody(xpath("//*[local-name()='Messaging']"))
                        .convertBodyTo(InputStream.class)
                        .process(xmlSchemaValidator)
                        .to(mockValid)
                    .doCatch(ValidationException.class)
                        .to(mockInvalid)
                .routeId("testValidator");
            }
        };
    }
}
