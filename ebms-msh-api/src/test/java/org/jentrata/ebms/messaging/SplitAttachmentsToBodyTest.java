package org.jentrata.ebms.messaging;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.junit.Test;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Unit test for org.jentrata.ebms.messaging.SplitAttachmentsToBody
 *
 * @author aaronwalker
 */
public class SplitAttachmentsToBodyTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockSplitter")
    protected MockEndpoint mockSplitter;

    @Test
    public void testDefaultSplitter() throws Exception {

        mockSplitter.setExpectedMessageCount(2);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder("originalHeader", "originalHeader", "originalHeader");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, "test", "test");
        mockSplitter.expectedBodiesReceivedInAnyOrder("attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        context().createProducerTemplate().send("direct:testDefaultSplitter",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testDontCopyMessageSplitter() throws Exception {

        mockSplitter.setExpectedMessageCount(2);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder("originalHeader", "originalHeader", "originalHeader");
        mockSplitter.expectedBodiesReceivedInAnyOrder("attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        context().createProducerTemplate().send("direct:testDontCopyMessageSplitter",request);

        assertMockEndpointsSatisfied();

        failIfHeaderExists(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY,mockSplitter.getExchanges());
    }

    @Test
    public void testDontCopyHeadersSplitter() throws Exception {

        mockSplitter.setExpectedMessageCount(2);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, "test", "test");
        mockSplitter.expectedBodiesReceivedInAnyOrder("attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        context().createProducerTemplate().send("direct:testDontCopyHeadersSplitter",request);

        assertMockEndpointsSatisfied();

        failIfHeaderExists("originalHeader",mockSplitter.getExchanges());
    }

    @Test
    public void testCopyCustomHeadersSplitter() throws Exception {

        mockSplitter.setExpectedMessageCount(2);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, "test", "test");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder("header1", "value1", "value1");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder("header2", "value2", "value2");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder("header3", "value3", "value3");
        mockSplitter.expectedBodiesReceivedInAnyOrder("attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        request.getIn().setHeader("header1","value1");
        request.getIn().setHeader("header2","value2");
        request.getIn().setHeader("header3","value3");
        request.getIn().setHeader("header4","value4");
        context().createProducerTemplate().send("direct:testCopyCustomHeadersSplitter",request);

        assertMockEndpointsSatisfied();

        failIfHeaderExists("originalHeader",mockSplitter.getExchanges());
        failIfHeaderExists("header4",mockSplitter.getExchanges());
    }

    @Test
    public void testIncludeOriginalMessageSplitter() throws Exception {

        mockSplitter.setExpectedMessageCount(3);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2",EbmsConstants.SOAP_BODY_PAYLOAD_ID);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain",EbmsConstants.TEXT_XML_CONTENT_TYPE);
        mockSplitter.expectedBodiesReceivedInAnyOrder("test","attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        context().createProducerTemplate().send("direct:testIncludeOriginalMessageSplitter",request);

        assertMockEndpointsSatisfied();

        failIfHeaderExists("originalHeader",mockSplitter.getExchanges());
        failIfHeaderExists(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY,mockSplitter.getExchanges());
    }

    @Test
    public void testIncludeOriginalMessageSplitterWithContentIDAndType() throws Exception {

        mockSplitter.setExpectedMessageCount(3);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, "attachment1", "attachment2","testCID");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/plain", "text/plain","text/plain");
        mockSplitter.expectedBodiesReceivedInAnyOrder("test","attachment1", "attachment2");

        Exchange request = getExchange("test","attachment1","attachment2");
        request.getIn().setHeader(EbmsConstants.CONTENT_ID,"testCID");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/plain");
        context().createProducerTemplate().send("direct:testIncludeOriginalMessageSplitter",request);

        assertMockEndpointsSatisfied();

        failIfHeaderExists("originalHeader",mockSplitter.getExchanges());
        failIfHeaderExists(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY,mockSplitter.getExchanges());
    }

    @Test
    public void testWithBodyOnly() throws Exception {
        mockSplitter.setExpectedMessageCount(1);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_ID, EbmsConstants.SOAP_BODY_PAYLOAD_ID);
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(EbmsConstants.CONTENT_TYPE, "text/xml");
        mockSplitter.expectedHeaderValuesReceivedInAnyOrder(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, "test");
        mockSplitter.expectedBodiesReceivedInAnyOrder("<test/>");

        Exchange request = getExchange("<test/>");
        request.getIn().setHeader(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY,"test");
        context().createProducerTemplate().send("direct:testWithBodyOnly",request);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:testDefaultSplitter")
                    .split(new SplitAttachmentsToBody())
                        .to(mockSplitter);

                from("direct:testDontCopyMessageSplitter")
                    .split(new SplitAttachmentsToBody(false))
                        .to(mockSplitter);

                from("direct:testDontCopyHeadersSplitter")
                    .split(new SplitAttachmentsToBody(true, false))
                        .to(mockSplitter);

                from("direct:testCopyCustomHeadersSplitter")
                    .split(new SplitAttachmentsToBody(true, false, "header1", "header2", "header3"))
                        .to(mockSplitter);

                from("direct:testIncludeOriginalMessageSplitter")
                    .split(new SplitAttachmentsToBody(false, false, true))
                        .to(mockSplitter);

                from("direct:testWithBodyOnly")
                    .split(new SplitAttachmentsToBody(true, false, true))
                        .to(mockSplitter);
            }
        };
    }

    private void failIfHeaderExists(String header, List<Exchange> exchanges) {
        for(Exchange exchange : exchanges) {
            if(exchange.getIn().getHeader(header) != null) {
                fail("message shouldn't contain header " + header + " but it does");
            }
        }
    }

    private Exchange getExchange(String body,String...attachments) {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(body);
        for(String attachment : attachments) {
            request.getIn().addAttachment(attachment,getAttachment(attachment));
        }
        request.getIn().setHeader("originalHeader","originalHeader");
        return request;
    }

    private DataHandler getAttachment(String id) {
        return new DataHandler(new InputStreamDataSource(new ByteArrayInputStream(id.getBytes()),"text/plain"));
    }
}
