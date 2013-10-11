package org.jentrata.ebms.as4.internal.routes;

import com.google.common.collect.ImmutableList;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.EbmsError;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.*;
import org.jentrata.ebms.cpa.pmode.BusinessInfo;
import org.jentrata.ebms.cpa.pmode.Protocol;
import org.jentrata.ebms.cpa.pmode.Service;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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

    private CPARepository cpaRepository;

    @Test
    public void testHasValidPartnerAgreement() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "service1");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"action1");
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT,Boolean.class),is(true));
    }

    @Test
    public void testHasInvalidPartnerAgreement() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testServiceInvalid");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION, "testAction");
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(request.isFailed(),equalTo(true));
        assertThat(request.getException(),instanceOf(InvalidPartnerAgreementException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT, Boolean.class),is(false));
    }

    @Test
    public void testNullServiceAndAction() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(request.isFailed(), equalTo(true));
        assertThat(request.getException(),instanceOf(InvalidPartnerAgreementException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.VALID_PARTNER_AGREEMENT,Boolean.class),is(false));
    }

    @Test
    public void testLookupCPAId() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.USER_MESSAGE);
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);

        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testLookupCPAIdForSignalMessage() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE);
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);

        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testLookupCPAIdForSignalMessageWithUserMessage() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage("simple-as4-receipt.xml"));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE);
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);

        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testInvalidLookupCPAId() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction2");
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);
        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(nullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo(EbmsConstants.CPA_ID_UNKNOWN));
    }

    @Test
    public void testDefaultLookupCPAId() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testService");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testAction2");
        request.getIn().setHeader(EbmsConstants.DEFAULT_CPA_ID,"testCPAId");
        Exchange response = context().createProducerTemplate().send("direct:lookupCpaId",request);
        assertThat(response.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class),is(notNullValue()));
        assertThat(response.getIn().getHeader(EbmsConstants.CPA_ID, String.class),equalTo("testCPAId"));
    }

    @Test
    public void testValidationErrors() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(loadEbmsMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMsgID");
        request.getIn().setHeader(EbmsConstants.REF_TO_MESSAGE_ID, "testRefMsgID");
        request.getIn().setHeader(EbmsConstants.MESSAGE_SERVICE, "testServiceValidation");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ACTION,"testActionValidation");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE, MessageType.USER_MESSAGE);
        request.getIn().setHeader(EbmsConstants.CPA,createPartnerAgreement());
        Exchange response = context().createProducerTemplate().send("direct:validatePartner",request);

        assertThat(response.isFailed(),is(true));
        assertThat(response.getException(),instanceOf(InvalidPartnerAgreementException.class));
        InvalidPartnerAgreementException exception = response.getException(InvalidPartnerAgreementException.class);
        assertThat(exception.getValidationErrors(),hasSize(1));
        assertThat(exception.getValidationErrors().get(0).getError(),equalTo(EbmsError.EBMS_0003));


    }

    private PartnerAgreement createPartnerAgreement() {
        PartnerAgreement agreement = new PartnerAgreement();
        agreement.setCpaId("validationErrors");
        BusinessInfo businessInfo = new BusinessInfo();
        agreement.setBusinessInfo(businessInfo);
        Service service = new Service("testServiceValidation","testActionValidation");
        ValidationPredicate predicate = new ValidationPredicate() {
            @Override
            public boolean matches(Exchange exchange) {
                exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,"message invalid....just because I don't like you");
                return false;
            }
        };
        service.setValidations(Arrays.asList(predicate));
        agreement.getBusinessInfo().setServices(Arrays.asList(service));
        return agreement;
    }

    private InputStream loadEbmsMessage() throws IOException {
        return loadEbmsMessage("sample-ebms-user-message.xml");
    }

    private InputStream loadEbmsMessage(String filename) throws IOException {
        return new ByteArrayInputStream(EbmsUtils.toStringFromClasspath(filename).getBytes());
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        cpaRepository = new MockCpaRepository();
        registry.bind("cpaRepository", cpaRepository);
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
            Protocol protocol = new Protocol();
            protocol.setAddress("http://example.jentrata.com");
            partnerAgreement.setProtocol(protocol);
            BusinessInfo businessInfo = new BusinessInfo();
            businessInfo.setServices(new ImmutableList.Builder<Service>()
                    .add(new Service("service", "action"))
                    .build()
            );
            partnerAgreement.setBusinessInfo(businessInfo);
            return partnerAgreement;
        }

        @Override
        public PartnerAgreement findByServiceAndAction(String service, String action) {
            switch (service + "|" + action) {
                case "service1|action1":
                    PartnerAgreement partnerAgreement = new PartnerAgreement();
                    partnerAgreement.setCpaId("testCPAId");
                    Protocol protocol = new Protocol();
                    protocol.setAddress("http://example.jentrata.com");
                    partnerAgreement.setProtocol(protocol);
                    BusinessInfo businessInfo = new BusinessInfo();
                    partnerAgreement.setBusinessInfo(businessInfo);
                    businessInfo.setServices(new ImmutableList.Builder<Service>()
                            .add(new Service(service, action))
                            .build()
                    );
                    return partnerAgreement;
                case "testServiceValidation|testActionValidation":
                   return createPartnerAgreement();
                default:
                    return null;
            }
        }

        @Override
        public PartnerAgreement findByMessage(Document message, String ebmsVersion) {
            try {
                String serviceValue = EbmsUtils.ebmsXpathValue(message.getDocumentElement(), "//eb3:CollaborationInfo/eb3:Service/text()");
                String actionValue = EbmsUtils.ebmsXpathValue(message.getDocumentElement(),"//eb3:CollaborationInfo/eb3:Action/text()");
                return findByServiceAndAction(serviceValue,actionValue);

            } catch (XPathExpressionException e) {
                throw new RuntimeException(e);
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
