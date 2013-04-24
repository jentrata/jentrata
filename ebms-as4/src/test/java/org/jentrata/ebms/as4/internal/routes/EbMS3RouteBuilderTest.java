package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.internal.messaging.MessageDetector;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import javax.xml.soap.SOAPConstants;
import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Unit tests for org.jentrata.ebms.as4.internal.routes.EbMS3RouteBuilder
 *
 * @author aaronwalker
 */
public class EbMS3RouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockEbmsInbound")
    protected MockEndpoint mockEbmsInbound;

    @Test
    public void testValidMultipartEBM3UserMessage() throws Exception {
        mockEbmsInbound.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"Multipart/Related; boundary=\"----=_Part_7_10584188.1123489648993\"; type=\"application/soap+xml\"; start=\"<soapPart@jentrata.org>\"");
        request.getIn().setHeader(Exchange.HTTP_METHOD,"POST");
        request.getIn().setBody(new FileInputStream(fileFromClasspath("simple-as4-user-message.txt")));
        Exchange response = context().createProducerTemplate().send("direct:testEbmsInbound",request);

        assertMockEndpointsSatisfied();

        //assert the response from the route
        assertThat("should have gotten http 204 response code",response.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE,Integer.class),equalTo(204));
        assertThat("should have gotten no content in the http response",response.getIn().getBody(),nullValue());

        Message msg = mockEbmsInbound.getExchanges().get(0).getIn();
        assertThat(msg.getBody(), notNullValue());
        assertThat(msg.getBody(),instanceOf(String.class));
        assertThat(msg.getHeader(EbmsConstants.SOAP_VERSION, String.class),equalTo(SOAPConstants.SOAP_1_2_PROTOCOL));
        assertThat(msg.getHeader(EbmsConstants.EBMS_VERSION,String.class),equalTo(EbmsConstants.EBMS_V3));

        String message = msg.getBody(String.class);
        assertThat(message,notNullValue());
        assertStringContains(message, "<S12:Envelope xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\"");
        assertThat(msg.hasAttachments(), is(Boolean.TRUE));
        assertThat(msg.getAttachmentNames().size(), equalTo(1));
        assertThat(msg.getAttachmentNames(),contains("<attachmentPart@jentrata.org>"));
    }

    @Test
    public void testValidEBM3ReceiptMessage() throws Exception {
        mockEbmsInbound.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(Exchange.HTTP_METHOD,"POST");
        request.getIn().setBody(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testEbmsInbound",request);

        assertMockEndpointsSatisfied();

        //assert the response from the route
        assertThat("should have gotten http 204 response code",response.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE,Integer.class),equalTo(204));
        assertThat("should have gotten no content in the http response",response.getIn().getBody(),nullValue());

        Message msg = mockEbmsInbound.getExchanges().get(0).getIn();
        assertThat(msg.getBody(), notNullValue());
        assertThat(msg.getBody(),instanceOf(String.class));
        assertThat(msg.getHeader(EbmsConstants.SOAP_VERSION,String.class),equalTo(SOAPConstants.SOAP_1_2_PROTOCOL));
        assertThat(msg.getHeader(EbmsConstants.EBMS_VERSION,String.class),equalTo(EbmsConstants.EBMS_V3));
        String message = msg.getBody(String.class);
        assertThat(message,notNullValue());
        assertStringContains(message, "<S12:Envelope xmlns:S12=\"http://www.w3.org/2003/05/soap-envelope\"");
    }

    @Test
    public void testInvalidHttpMethod() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(Exchange.HTTP_METHOD,"GET");
        request.getIn().setBody(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testEbmsInbound",request);

        assertThat("should have gotten http 405 response code",response.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE,Integer.class),equalTo(405));
        assertThat("should have gotten no content in the http response",response.getIn().getBody(String.class),notNullValue());
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        EbMS3RouteBuilder routeBuilder = new EbMS3RouteBuilder();
        routeBuilder.setEbmsHttpEndpoint("direct:testEbmsInbound");
        routeBuilder.setInboundEbmsQueue(mockEbmsInbound.getEndpointUri());
        routeBuilder.setMessageDetector(new MessageDetector());
        return new RouteBuilder[] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT)
                            .log(LoggingLevel.INFO, "Storing message no where ${headers}")
                        .routeId("mockStoreMessage");

                        from("direct:validatePartner")
                            .setHeader("JentrataIsValidTradingPartner", constant(Boolean.TRUE))
                            .setHeader("JentrataMEP", constant("One-Way"))
                        .routeId("mockValidatePartner");
                    }
                }
        };
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
