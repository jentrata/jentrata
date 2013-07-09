package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.EbmsError;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.messaging.UUIDGenerator;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.hamcrest.Matchers.hasXPath;

/**
 * Unit tests for EbmsErrorHandlerRouteBuilder
 *
 * @author aaronwalker
 */
public class EbmsErrorHandlerRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockEbmsOutbound")
    protected MockEndpoint mockEbmsOutbound;

    @EndpointInject(uri = "mock:mockMessageStore")
    protected MockEndpoint mockMessageStore;

    @EndpointInject(uri = "mock:mockUpdateMessageStore")
    protected MockEndpoint mockUpdateMessageStore;

    @Test
    public void testWithNoErrorCode() throws Exception {
        assertEbmsError("test1234@jentrata.org",null,null);
    }

    @Test
    public void testWithErrorCode() throws Exception {
        assertEbmsError("test1234jentrata.org",EbmsError.EBMS_0101,null);
    }

    @Test
    public void testWithUnsafeCharacters() throws Exception {
        assertEbmsError("test1234&jentrata.org",EbmsError.EBMS_0101,"\"invalid &'<> chars\"");
    }

    @Test
    public void testWitErrorCodeAndDescription() throws Exception {
        Exchange response = assertEbmsError("test1234@jentrata.org",EbmsError.EBMS_0101,"signature verification failure");
        Document error = response.getIn().getBody(Document.class);
        assertThat(error,hasXPath("//*[local-name()='Description' and text()='signature verification failure']"));
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("uuidGenerator", new UUIDGenerator());
        return registry;
    }

    @Override
    protected RouteBuilder [] createRouteBuilders() throws Exception {
        EbmsErrorHandlerRouteBuilder routeBuilder = new EbmsErrorHandlerRouteBuilder();
        routeBuilder.setErrorQueue("direct:testError");
        routeBuilder.setOutboundEbmsQueue(mockEbmsOutbound.getEndpointUri());
        routeBuilder.setMessgeStoreEndpoint(mockMessageStore.getEndpointUri());
        routeBuilder.setMessageInsertEndpoint(mockUpdateMessageStore.getEndpointUri());
        return new RouteBuilder[] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:validateEbms")
                            .setBody(xpath("//*[local-name()='Messaging']").nodeResult())
                            .to("validator:schemas/ebms-header-3_0-200704.xsd")
                            .setHeader("schemaValid",constant(true))
                        .routeId("testEbmsValidator");
                    }
                }
        };
    }

    private Exchange assertEbmsError(String messageId,EbmsError ebmsError, String description) throws Exception {

        mockEbmsOutbound.setExpectedMessageCount(1);
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);

        mockMessageStore.expectedHeaderReceived(EbmsConstants.REF_TO_MESSAGE_ID,messageId);
        mockMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);
        mockMessageStore.expectedHeaderReceived(EbmsConstants.CONTENT_TYPE,EbmsConstants.SOAP_XML_CONTENT_TYPE);
        mockMessageStore.expectedHeaderReceived(EbmsConstants.CPA_ID,"testCPAId");

        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.REF_TO_MESSAGE_ID, messageId);
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_DIRECTION, EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_STATUS, MessageStatusType.DELIVER);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,messageId);
        request.getIn().setHeader(EbmsConstants.MESSAGE_RECEIPT_PATTERN, Security.ReplyPatternType.Callback.name());

        if(ebmsError == null) {
            ebmsError = EbmsError.EBMS_0004; //default error code
        } else {
            request.getIn().setHeader(EbmsConstants.EBMS_ERROR_CODE,ebmsError.getErrorCode());
        }

        if(description != null) {
            request.getIn().setHeader(EbmsConstants.EBMS_ERROR_DESCRIPTION,description);
        }

        Exchange response = context().createProducerTemplate().send("direct:testError",request);

        assertMockEndpointsSatisfied();
        validateEbmsMessage(response,ebmsError);

        return response;
    }

    private void validateEbmsMessage(Exchange exchange, EbmsError ebmsError) {
        try {
            System.out.println(exchange.getIn().getBody());
            context().createProducerTemplate().sendBody("direct:validateEbms",exchange.getIn().getBody());
            Document error = exchange.getIn().getBody(Document.class);
            assertThat(error,hasXPath("//*[local-name()='Timestamp']"));
            assertThat(error,hasXPath("//*[local-name()='MessageId']"));
            assertThat(error,hasXPath("//@*[name()='origin' and .='" + ebmsError.getOrigin() + "']"));
            assertThat(error,hasXPath("//@*[name()='category' and .='" + ebmsError.getCategory() + "']"));
            assertThat(error,hasXPath("//@*[name()='errorCode' and .='" + ebmsError.getErrorCode() + "']"));
            assertThat(error,hasXPath("//@*[name()='severity' and .='" + ebmsError.getSeverity() + "']"));
            assertThat(error,hasXPath("//@*[name()='shortDescription' and .='" + ebmsError.getShortDescription() + "']"));
            assertThat(error,hasXPath("//@*[name()='refToMessageInError' and .='" + exchange.getIn().getHeader("JentrataRefToMessageInError") + "']"));
        } catch (Exception ex) {
            fail("invalid ebms message:" + ex.getMessage());
        }

    }

}
