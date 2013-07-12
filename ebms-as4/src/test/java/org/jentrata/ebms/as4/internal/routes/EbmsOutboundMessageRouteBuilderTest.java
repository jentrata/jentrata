package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.BusinessInfo;
import org.jentrata.ebms.messaging.UUIDGenerator;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Unit test for EbmsOutboundMessageRouteBuilder
 *
 * @author aaronwalker
 */
@SuppressWarnings("unchecked")
public class EbmsOutboundMessageRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockEbmsOutbound")
    protected MockEndpoint mockEbmsOutbound;

    @EndpointInject(uri = "mock:mockMessageStore")
    protected MockEndpoint mockMessageStore;

    @EndpointInject(uri = "mock:mockUpdateMessageStore")
    protected MockEndpoint mockUpdateMessageStore;

    @EndpointInject(uri = "mock:mockWSSEAddSecurityToHeader")
    protected MockEndpoint mockWSSEAddSecurityToHeader;

    @Test
    public void testWrapPayloadAsMimeMessage() throws Exception {

        mockEbmsOutbound.setExpectedMessageCount(1);
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockWSSEAddSecurityToHeader.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"987654321");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/xml");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_ID,"testpayload@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"MESSAGE_CONVERSATION_ID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,"http://jentrata.org/schema/example");
        request.getIn().setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,"http://jentrata.org/agreement");

        request.getIn().setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,"PartID=testpayload@jentrata.org;SourceABN=123456789;test=");

        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setBody(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();

        Message msg = mockEbmsOutbound.getExchanges().get(0).getIn();
        System.out.println(msg.getBody(String.class));
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
        assertThat(soapHeader.getOwnerDocument(),not(hasXPath("//@name[.='test']")));

        assertThat(message.countAttachments(), equalTo(1));
        AttachmentPart part = (AttachmentPart) message.getAttachments().next();
        assertThat(part.getContentId(),equalTo("<testpayload@jentrata.org>"));
        assertThat(part.getContentType(),equalTo("text/xml"));
        assertThat(part.getMimeHeader(EbmsConstants.CONTENT_TRANSFER_ENCODING)[0],equalTo("binary"));
        assertThat(part.getMimeHeader(EbmsConstants.CONTENT_DISPOSITION)[0].matches("attachment;\\sfilename=(.*)\\.xml"),is(true));
    }

    @Test
    public void testWrapPayloadAsMimeMessageWithCustomMimeHeaders() throws Exception {

        mockEbmsOutbound.setExpectedMessageCount(1);
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockWSSEAddSecurityToHeader.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"987654321");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/xml");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_ID,"testpayload@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"MESSAGE_CONVERSATION_ID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,"http://jentrata.org/schema/example");
        request.getIn().setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,"http://jentrata.org/agreement");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_FILENAME,"test.xml");
        request.getIn().setHeader(EbmsConstants.CONTENT_TRANSFER_ENCODING,"base64");


        request.getIn().setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,"PartID=testpayload@jentrata.org;SourceABN=123456789;test=");

        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setBody(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();

        Message msg = mockEbmsOutbound.getExchanges().get(0).getIn();
        System.out.println(msg.getBody(String.class));
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
        assertThat(soapHeader.getOwnerDocument(),not(hasXPath("//@name[.='test']")));

        assertThat(message.countAttachments(), equalTo(1));
        AttachmentPart part = (AttachmentPart) message.getAttachments().next();
        assertThat(part.getContentId(),equalTo("<testpayload@jentrata.org>"));
        assertThat(part.getContentType(),equalTo("text/xml"));
        assertThat(part.getMimeHeader(EbmsConstants.CONTENT_TRANSFER_ENCODING)[0],equalTo("base64"));
        assertThat(part.getMimeHeader(EbmsConstants.CONTENT_DISPOSITION)[0],equalTo("attachment; filename=test.xml"));
    }

    @Test
    public void testPayloadCompressionUsingOverrideHeader() throws Exception {
        mockEbmsOutbound.setExpectedMessageCount(1);
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockWSSEAddSecurityToHeader.setExpectedMessageCount(1);

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
        request.getIn().setHeader(EbmsConstants.PAYLOAD_COMPRESSION,EbmsConstants.GZIP);

        byte [] expectedPayload = IOUtils.toByteArray(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        request.getIn().setBody(expectedPayload);
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();

        SOAPMessage soapMessage = EbmsUtils.parse(mockEbmsOutbound.getExchanges().get(0));
        SOAPHeader soapHeader = soapMessage.getSOAPPart().getEnvelope().getHeader();
        System.out.println(EbmsUtils.toString(soapMessage));

        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='Timestamp']"));
        assertThat(soapHeader.getOwnerDocument(), hasXPath("//*[local-name()='MessageId']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='PartyId' and text()='123456789']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='PartyId' and text()='987654321']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='AgreementRef' and text()='http://jentrata.org/agreement']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//*[local-name()='Schema' and @*[local-name()='location']='http://jentrata.org/schema/example']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='MimeType' and ../text()='text/xml']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='CharacterSet' and ../text()='UTF-8']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='PartID' and ../text()='testpayload@jentrata.org']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='SourceABN' and ../text()='123456789']"));
        assertThat(soapHeader.getOwnerDocument(),hasXPath("//@name[.='CompressionType' and ../text()='application/gzip']"));


        assertThat(soapMessage.countAttachments(),equalTo(1));
        Iterator<AttachmentPart> attachments = soapMessage.getAttachments();
        while (attachments.hasNext()) {
            AttachmentPart part = attachments.next();
            assertThat(part.getContentId(),equalTo("<testpayload@jentrata.org>"));
            assertThat(part.getContentType(),equalTo(EbmsConstants.GZIP));
            byte [] payload = EbmsUtils.decompress(EbmsConstants.GZIP,part.getRawContentBytes());
            System.out.println(new String(payload));
            assertThat(payload,equalTo(expectedPayload));
        }
    }

    @Test
    public void testUnknownCPA() throws Exception {
        mockEbmsOutbound.setExpectedMessageCount(0);
        mockMessageStore.setExpectedMessageCount(0);
        mockUpdateMessageStore.setExpectedMessageCount(0);
        mockWSSEAddSecurityToHeader.setExpectedMessageCount(0);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"987654321");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/xml");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"crappyCPAID");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_ID,"testpayload@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"MESSAGE_CONVERSATION_ID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,"http://jentrata.org/schema/example");
        request.getIn().setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,"http://jentrata.org/agreement");

        request.getIn().setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,"PartID=testpayload@jentrata.org;SourceABN=123456789");

        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setHeader("test","unknownCPA");

        request.getIn().setBody(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testOverrideMessageID() throws Exception {
        mockEbmsOutbound.setExpectedMessageCount(1);
        mockEbmsOutbound.expectedHeaderReceived(EbmsConstants.MESSAGE_ID, "test-exchange-id@jentrata.org");
        mockMessageStore.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(1);
        mockWSSEAddSecurityToHeader.setExpectedMessageCount(1);

        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"test-exchange-id@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"987654321");
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,"text/xml");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.PAYLOAD_ID,"testpayload@jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"MESSAGE_CONVERSATION_ID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,"http://jentrata.org/schema/example");
        request.getIn().setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,"http://jentrata.org/agreement");

        request.getIn().setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,"PartID=testpayload@jentrata.org;SourceABN=123456789;test=");

        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION, EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setBody(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Exchange response = context().createProducerTemplate().send("direct:testDeliveryQueue",request);

        assertMockEndpointsSatisfied();

        Exchange exchange = mockEbmsOutbound.getExchanges().get(0);
        System.out.println(exchange.getIn().getBody());
        SOAPMessage message = EbmsUtils.parse(exchange);
        Document ebmsMessage = message.getSOAPPart().getEnvelope().getHeader().getOwnerDocument();
        assertThat(ebmsMessage, hasXPath("//*[local-name()='MessageId' and text()='test-exchange-id@jentrata.org']"));
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("uuidGenerator", new UUIDGenerator());
        return registry;
    }

    @Override
    protected RouteBuilder [] createRouteBuilders() throws Exception {
        EbmsOutboundMessageRouteBuilder routeBuilder = new EbmsOutboundMessageRouteBuilder();
        routeBuilder.setDeliveryQueue("direct:testDeliveryQueue");
        routeBuilder.setOutboundEbmsQueue(mockEbmsOutbound.getEndpointUri());
        routeBuilder.setMessgeStoreEndpoint(mockMessageStore.getEndpointUri());
        routeBuilder.setMessageInsertEndpoint(mockUpdateMessageStore.getEndpointUri());
        routeBuilder.setWsseSecurityAddEndpoint(mockWSSEAddSecurityToHeader.getEndpointUri());
        return new RouteBuilder [] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:lookupCpaId")
                        .choice()
                            .when(header("test").isEqualTo("unknownCPA"))
                                .setHeader(EbmsConstants.CPA, constant(null))
                                .setHeader(EbmsConstants.CPA_ID, constant(EbmsConstants.CPA_ID_UNKNOWN))
                            .otherwise()
                                .setHeader(EbmsConstants.CPA,constant(getAgreement()))
                        .routeId("mockLookupCpaId");
                    }
                }
        };
    }

    private PartnerAgreement getAgreement() {
        PartnerAgreement partnerAgreement = new PartnerAgreement();
        partnerAgreement.setCpaId("testCPAId");
        partnerAgreement.setBusinessInfo(new BusinessInfo());
        return partnerAgreement;
    }

    protected static File fileFromClasspath(String filename) {
        return EbmsUtils.fileFromClasspath(filename);
    }
}
