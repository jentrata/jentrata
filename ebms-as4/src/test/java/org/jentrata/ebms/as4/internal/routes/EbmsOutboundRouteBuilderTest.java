package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;

/**
 * Unit tests for org.jentrata.ebms.as4.internal.routes.EbmsOutboundRouteBuilder
 *
 * @author aaronwalker
 */
public class EbmsOutboundRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:agreement1")
    protected MockEndpoint mockAgreement1;


    @EndpointInject(uri = "mock:agreement2")
    protected MockEndpoint mockAgreement2;

    @EndpointInject(uri = "mock:mockUpdateMessageStore")
    protected MockEndpoint mockUpdateMessageStore;

    @EndpointInject(uri = "mock:mockEbmsResponseInbound")
    protected MockEndpoint mockEbmsResponseInbound;

    @Test
    public void testSendMessageToPartner() throws Exception {

        mockAgreement1.setExpectedMessageCount(1);
        mockAgreement2.setExpectedMessageCount(1);
        mockUpdateMessageStore.setExpectedMessageCount(4);
        mockEbmsResponseInbound.setExpectedMessageCount(1);
        mockEbmsResponseInbound.expectedBodiesReceived(IOUtils.toString(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml"))));
        mockEbmsResponseInbound.expectedHeaderReceived(EbmsConstants.CONTENT_TYPE,EbmsConstants.SOAP_XML_CONTENT_TYPE);
        mockEbmsResponseInbound.expectedHeaderReceived(EbmsConstants.CPA_ID,"agreement2");
        mockEbmsResponseInbound.expectedHeaderReceived(Exchange.HTTP_METHOD,"POST");


        sendMessage("agreement1",
                "simple-as4-user-message.txt",
                "Multipart/Related; boundary=\"----=_Part_7_10584188.1123489648993\"; type=\"application/soap+xml\"; start=\"<soapPart@jentrata.org>\"",
                "2011-921@5209999001264.jentrata.org",
                MessageType.USER_MESSAGE);

        sendMessage("agreement2",
                "simple-as4-receipt.xml",
                EbmsConstants.SOAP_XML_CONTENT_TYPE,
                "someuniqueid@receiver.jentrata.org",
                MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE);

        assertMockEndpointsSatisfied();

        Exchange e = mockEbmsResponseInbound.getExchanges().get(0);
        assertThat(e.getIn().getHeaders().values(),hasSize(3));
    }

    private void sendMessage(String cpaId, String filename, String contentType, String msgId, MessageType type) throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,contentType);
        request.getIn().setHeader(EbmsConstants.CPA_ID,cpaId);
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,msgId);
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, type);
        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_OUTBOUND);

        request.getIn().setBody(new FileInputStream(fileFromClasspath("simple-as4-user-message.txt")));
        Exchange response = context().createProducerTemplate().send("direct:testOutboundEbmsQueue",request);


    }

    @Override
    protected RouteBuilder [] createRouteBuilders() throws Exception {
        EbmsOutboundRouteBuilder routeBuilder = new EbmsOutboundRouteBuilder();
        routeBuilder.setOutboundEbmsQueue("direct:testOutboundEbmsQueue");
        routeBuilder.setEbmsResponseInbound(mockEbmsResponseInbound.getEndpointUri());
        routeBuilder.setMessageUpdateEndpoint(mockUpdateMessageStore.getEndpointUri());
        routeBuilder.setCpaRepository(new DummyCPARepository());
        return new RouteBuilder[] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:agreement1")
                            .to(mockAgreement1.getEndpointUri())
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                            .setBody(constant(null))
                        .routeId("mockAgreement1");
                        from("direct:agreement2")
                            .to(mockAgreement2.getEndpointUri())
                            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                            .setBody(constant(IOUtils.toString(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml")))))
                        .routeId("mockAgreement2");

                        from(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
                            .log(LoggingLevel.INFO, "mock event notification: ${headers}")
                        .routeId("mockEventNotification");
                    }
                }
        };
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }

    private class DummyCPARepository implements CPARepository {
        @Override
        public List<PartnerAgreement> getPartnerAgreements() {
            return Collections.emptyList();
        }

        @Override
        public List<PartnerAgreement> getActivePartnerAgreements() {
            PartnerAgreement agreement1 = new PartnerAgreement();
            agreement1.setCpaId("agreement1");
            agreement1.setTransportReceiverEndpoint("direct:agreement1");
            PartnerAgreement agreement2 = new PartnerAgreement();
            agreement2.setCpaId("agreement2");
            agreement2.setTransportReceiverEndpoint("direct:agreement2");
            return Arrays.asList(agreement1,agreement2);
        }

        @Override
        public PartnerAgreement findByCPAId(String cpaId) {
            return null;
        }

        @Override
        public PartnerAgreement findByServiceAndAction(String service, String action) {
            return null;
        }

        @Override
        public PartnerAgreement findByMessage(Document message, String ebmsVersion) {
            return null;
        }


        @Override
        public boolean isValidPartnerAgreement(Map<String, Object> fields) {
            return true;
        }
    }
}
