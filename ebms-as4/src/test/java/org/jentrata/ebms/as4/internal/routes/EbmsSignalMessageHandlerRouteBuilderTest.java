package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Unit tests for EbmsSignalMessageHandlerRouteBuilder
 *
 * @author aaronwalker
 */
public class EbmsSignalMessageHandlerRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockUpdateMessageStore")
    protected MockEndpoint mockUpdateMessageStore;

    @Test
    public void testIncomingSignalMessage() throws Exception {

        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_ID,"orders123@buyer.jentrata.org");
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_STATUS, MessageStatusType.DONE);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE.name());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"someuniqueid@receiver.jentrata.org");
        request.getIn().setHeader(EbmsConstants.REF_TO_MESSAGE_ID,"orders123@buyer.jentrata.org");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");

        request.getIn().setBody(fileFromClasspath("simple-as4-receipt.xml"));
        Exchange response = context().createProducerTemplate().send("direct:test",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testIncomingSignalMessageWithError() throws Exception {

        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_ID, "f2f60e33-f0e7-469b-8c2b-79b615fd6b21@jentrata.org");
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_STATUS, MessageStatusType.ERROR);
        mockUpdateMessageStore.expectedHeaderReceived(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, "failed authentication");

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE_ERROR.name());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"9e81f6b8-c02c-4d43-91cf-d160983fa957@jentrata.org");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");

        request.getIn().setBody(fileFromClasspath("simple-as4-error.xml"));
        Exchange response = context().createProducerTemplate().send("direct:test",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testIncomingSignalMessageUnknownCPAId() throws Exception {

        mockUpdateMessageStore.setExpectedMessageCount(0);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE_ERROR.name());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"9e81f6b8-c02c-4d43-91cf-d160983fa957@jentrata.org");
        request.getIn().setHeader(EbmsConstants.CPA_ID,EbmsConstants.CPA_ID_UNKNOWN);

        request.getIn().setBody(fileFromClasspath("simple-as4-error.xml"));
        Exchange response = context().createProducerTemplate().send("direct:test",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testIncomingSignalMessageUnknownMessageType() throws Exception {

        mockUpdateMessageStore.setExpectedMessageCount(0);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/soap+xml");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.USER_MESSAGE.name());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"9e81f6b8-c02c-4d43-91cf-d160983fa957@jentrata.org");
        request.getIn().setHeader(EbmsConstants.CPA_ID,EbmsConstants.CPA_ID_UNKNOWN);

        request.getIn().setBody(fileFromClasspath("simple-as4-error.xml"));
        Exchange response = context().createProducerTemplate().send("direct:test",request);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        EbmsSignalMessageHandlerRouteBuilder routeBuilder = new EbmsSignalMessageHandlerRouteBuilder();
        routeBuilder.setInboundEbmsSignalsQueue("direct:test");
        routeBuilder.setMessageUpdateEndpoint(mockUpdateMessageStore.getEndpointUri());
        return routeBuilder;
    }

    protected static String fileFromClasspath(String filename) throws IOException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return IOUtils.toString(new FileInputStream(file));
    }
}
