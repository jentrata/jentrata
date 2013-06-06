package org.jentrata.ebms.as4.internal.routes;

import com.google.common.collect.ImmutableList;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.InvalidPartnerAgreementException;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.Service;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for org.jentrata.ebms.as4.internal.routes.ValidatePartnerAgreementRouteBuilder
 *
 * @author aaronwalker
 */
public class ValidatePartnerAgreementRouteBuilderTest extends CamelTestSupport {

    @Test
    public void testHasValidPartnerAgreement() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction");
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT,Boolean.class),is(true));
    }

    @Test
    public void testHasInvalidPartnerAgreement() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testServiceInvalid");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION, "testAction");
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(request.isFailed(),equalTo(true));
        assertThat(request.getException(),instanceOf(InvalidPartnerAgreementException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT, Boolean.class),is(false));
    }

    @Test
    public void testNullServiceAndAction() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(request.isFailed(), equalTo(true));
        assertThat(request.getException(),instanceOf(InvalidPartnerAgreementException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT,Boolean.class),is(false));
    }

    @Test
    public void testLookupCPAId() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.USER_MESSAGE);
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);

        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testLookupCPAIdForSignalMessage() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE);
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);

        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testInvalidLookupCPAId() {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction2");
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);
        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(nullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo(EbmsConstants.CPA_ID_UNKNOWN));
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        registry.bind("cpaRepository",new MockCpaRepository());
        registry.bind("messageStore", mockMessageStore());
        return registry;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        ValidatePartnerAgreementRouteBuilder routeBuilder = new ValidatePartnerAgreementRouteBuilder();
        return routeBuilder;
    }

    private class MockCpaRepository implements CPARepository {

        @Override
        public List<PartnerAgreement> getPartnerAgreements() {
            return null;
        }

        @Override
        public List<PartnerAgreement> getActivePartnerAgreements() {
            return null;
        }

        @Override
        public PartnerAgreement findByCPAId(@Header(EbmsConstants.CPA_ID) String cpaId) {
            PartnerAgreement partnerAgreement = new PartnerAgreement();
            partnerAgreement.setCpaId("testCPAId");
            partnerAgreement.setTransportReceiverEndpoint("http://example.jentrata.com");
            partnerAgreement.setServices(new ImmutableList.Builder<Service>()
                    .add(new Service("service", "action"))
                    .build()
            );
            return partnerAgreement;
        }

        @Override
        public PartnerAgreement findByServiceAndAction(String service, String action) {
            switch (service + "|" + action) {
                case "testService|testAction":
                    PartnerAgreement partnerAgreement = new PartnerAgreement();
                    partnerAgreement.setCpaId("testCPAId");
                    partnerAgreement.setTransportReceiverEndpoint("http://example.jentrata.com");
                    partnerAgreement.setServices(new ImmutableList.Builder<Service>()
                            .add(new Service(service, action))
                            .build()
                    );
                    return partnerAgreement;
                default:
                    return null;
            }
        }

        @Override
        public boolean isValidPartnerAgreement(Map<String, Object> fields) {
            String service = (String) fields.get(EbmsConstants.MESSAGE_SERVICE);
            String action = (String) fields.get(EbmsConstants.MESSAGE_ACTION);
            PartnerAgreement agreement = findByServiceAndAction(service,action);
            return  agreement != null;
        }

    }

    private MessageStore mockMessageStore() {
        MessageStore mock = mock(MessageStore.class);
        Message message = mock(Message.class);
        doReturn("testCPAId").when(message).getCpaId();
        doReturn(message).when(mock).findByMessageId(anyString(),anyString());
        return mock;
    }
}
