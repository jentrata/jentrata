package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.messaging.UUIDGenerator;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasXPath;

/**
 * Unit test for org.jentrata.ebms.as4.internal.routes.EbmsSignalMessageRouteBuilder
 *
 * @author aaronwalker
 */
public class EbmsSignalMessageRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockEbmsOutbound")
    protected MockEndpoint mockEbmsOutbound;

    @Test
    public void testGenerateValidReceiptSignalMessage() throws Exception{

        mockEbmsOutbound.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"192837465");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"orders123@buyer.jentrata.org");

        request.getIn().setBody(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml")));
        context().createProducerTemplate().send("direct:testGenerateReceipt",request);

        assertMockEndpointsSatisfied();

        Exchange receipt = mockEbmsOutbound.getExchanges().get(0);

        assertThat(receipt, notNullValue());
        assertThat(receipt.getIn().getBody(Document.class), hasXPath("//*[local-name()='SignalMessage']"));
        assertThat(receipt.getIn().getBody(Document.class), hasXPath("//*[local-name()='MessageId' and contains(text(), '@jentrata.org')]"));
        assertThat(receipt.getIn().getBody(Document.class), hasXPath("//*[local-name()='RefToMessageId' and text()='orders123@buyer.jentrata.org']"));

        assertThat(receipt.getIn().getHeader(EbmsConstants.MESSAGE_TYPE,String.class), equalTo(MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE.name()));
        assertThat(receipt.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class), endsWith("@jentrata.org"));
        assertThat(receipt.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION, String.class), equalTo(EbmsConstants.MESSAGE_DIRECTION_OUTBOUND));
        assertThat(receipt.getIn().getHeader(EbmsConstants.CONTENT_TYPE, String.class), equalTo(EbmsConstants.SOAP_XML_CONTENT_TYPE));
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("uuidGenerator", new UUIDGenerator());
        return registry;
    }

    @Override
    protected RouteBuilder [] createRouteBuilders() throws Exception {
        EbmsSignalMessageRouteBuilder routeBuilder = new EbmsSignalMessageRouteBuilder();
        routeBuilder.setInboundEbmsQueue("direct:testGenerateReceipt");
        routeBuilder.setOutboundEbmsQueue(mockEbmsOutbound.getEndpointUri());
        return new RouteBuilder[] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT)
                            .log(LoggingLevel.INFO, "Storing message no where ${headers}")
                        .routeId("mockStoreMessage");
                    }
                }
        };
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }

}
