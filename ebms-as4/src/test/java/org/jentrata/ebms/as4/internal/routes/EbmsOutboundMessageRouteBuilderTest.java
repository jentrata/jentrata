package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.UUIDGenerator;
import org.jentrata.ebms.soap.SoapMessageDataFormat;
import org.junit.Test;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasXPath;

/**
 * Unit test for EbmsOutboundMessageRouteBuilder
 *
 * @author aaronwalker
 */
public class EbmsOutboundMessageRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockEbmsOutbound")
    protected MockEndpoint mockEbmsOutbound;

    @EndpointInject(uri = "mock:mockMessageStore")
    protected MockEndpoint mockMessageStore;

    @EndpointInject(uri = "mock:mockUpdateMessageStore")
    protected MockEndpoint mockUpdateMessageStore;

    @Test
    public void testWrapPayloadAsMimeMessage() throws Exception {

        mockEbmsOutbound.setExpectedMessageCount(1);
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"987654321");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/xml");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_ID,"testpayload@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"MESSAGE_CONVERSATION_ID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,"http://jentrata.org/schema/example");
        request.getIn().setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,"http://jentrata.org/agreement");

        request.getIn().setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,"PartID=testpayload@jentrata.org;SourceABN=123456789");

        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setBody(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();

        Message msg = mockEbmsOutbound.getExchanges().get(0).getIn();
        System.out.println(msg.getBody());
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_2_PROTOCOL);
        mimeHeaders.addHeader(Exchange.CONTENT_TYPE, msg.getHeader(Exchange.CONTENT_TYPE, String.class));
        SOAPMessage message = messageFactory.createMessage(mimeHeaders, msg.getBody(InputStream.class));
        SOAPHeader soapHeader = message.getSOAPPart().getEnvelope().getHeader();

        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='Timestamp']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='MessageId']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='PartyId' and text()='123456789']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='PartyId' and text()='987654321']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='AgreementRef' and text()='http://jentrata.org/agreement']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='Schema' and @*[local-name()='location']='http://jentrata.org/schema/example']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='MimeType' and ../text()='text/xml']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='CharacterSet' and ../text()='UTF-8']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='PartID' and ../text()='testpayload@jentrata.org']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='SourceABN' and ../text()='123456789']"));
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("uuidGenerator", new UUIDGenerator());
        return registry;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        EbmsOutboundMessageRouteBuilder routeBuilder = new EbmsOutboundMessageRouteBuilder();
        routeBuilder.setDeliveryQueue("direct:testDeliveryQueue");
        routeBuilder.setOutboundEbmsQueue(mockEbmsOutbound.getEndpointUri());
        routeBuilder.setMessgeStoreEndpoint(mockMessageStore.getEndpointUri());
        routeBuilder.setMessageInsertEndpoint(mockUpdateMessageStore.getEndpointUri());
        return routeBuilder;
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
