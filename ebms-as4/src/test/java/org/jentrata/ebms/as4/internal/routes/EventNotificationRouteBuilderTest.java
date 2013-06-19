package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.codehaus.jackson.map.ObjectMapper;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for org.jentrata.ebms.as4.internal.routes.EventNotificationRouteBuilder
 *
 * @author aaronwalker
 */
public class EventNotificationRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockNotification")
    protected MockEndpoint mockNotification;

    @EndpointInject(uri = "mock:mockOther")
    protected MockEndpoint mockOther;

    @Test
    public void testFireEvent() throws Exception {
        mockNotification.setExpectedMessageCount(1);

        Exchange request = createRequest();
        context().createProducerTemplate().send(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT,request);

        assertMockEndpointsSatisfied();
        assertEventNotification(mockNotification.getExchanges().get(0));
    }

    @Test
    public void testFireEventMinimal() throws Exception {
        mockNotification.setExpectedMessageCount(1);

        Exchange request = createRequest();
        request.getIn().removeHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION);
        request.getIn().removeHeader(EbmsConstants.REF_TO_MESSAGE_ID);
        request.getIn().removeHeader(EbmsConstants.MESSAGE_DATE);
        request.getIn().removeHeader(EbmsConstants.MESSAGE_CONVERSATION_ID);

        context().createProducerTemplate().send(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT,request);

        assertMockEndpointsSatisfied();

        Exchange event = mockNotification.getExchanges().get(0);
        assertThat(event.getIn().getBody(),notNullValue());
        System.out.println(event.getIn().getBody());
        Map<String,Object> message = fromJson(event.getIn().getBody(String.class));
        assertThat(message,notNullValue());
        assertThat((String) message.get("messageId"),equalTo("orders123@buyer.jentrata.org"));
        assertThat((String) message.get("direction"),equalTo(EbmsConstants.MESSAGE_DIRECTION_INBOUND));
        assertThat((String) message.get("cpaId"),equalTo("testCPAId"));
        assertThat((String) message.get("refMessageId"),equalTo(""));
        assertThat((String) message.get("conversationId"),equalTo(""));
        assertThat((String) message.get("status"),equalTo("RECEIVED"));
        assertThat((String) message.get("statusDescription"),equalTo(""));
        assertThat((String) message.get("messageDate"),equalTo(""));
        Map<String,Object> headers = (Map<String, Object>) message.get("headers");
        assertThat((String) headers.get("JentrataFrom"),equalTo("123456789"));
        assertThat((String) headers.get("JentrataTo"),equalTo("192837465"));
        assertThat((String) headers.get("jentrataVersion"),equalTo("TEST"));
    }

    @Test
    public void testFireEventWithComplexHeader() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        mockNotification.setExpectedMessageCount(1);

        Exchange request = createRequest();
        Date date = new Date();
        request.getIn().setHeader("complexDate", date);
        request.getIn().setHeader("complexList", Collections.emptyList());
        context().createProducerTemplate().send(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT,request);

        assertMockEndpointsSatisfied();
        Map<String,Object> event = assertEventNotification(mockNotification.getExchanges().get(0));
        Map<String,Object> headers = (Map<String, Object>) event.get("headers");
        assertThat((String) headers.get("complexDate"),startsWith(df.format(date)));
        assertThat(headers.get("complexList"),nullValue());

    }

    @Test
    public void testFireEventWithNullHeader() throws Exception {
        mockNotification.setExpectedMessageCount(1);

        Exchange request = createRequest();
        request.getIn().setHeader("nullHeader", null);
        context().createProducerTemplate().send(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT,request);

        assertMockEndpointsSatisfied();
        Map<String,Object> event = assertEventNotification(mockNotification.getExchanges().get(0));
        Map<String,Object> headers = (Map<String, Object>) event.get("headers");
        assertThat(headers.get("nullHeader"),nullValue());

    }

    @Test
    public void testWireTrapEventNotification() throws Exception {
        mockNotification.setExpectedMessageCount(1);
        mockOther.setExpectedMessageCount(1);
        mockOther.expectedBodiesReceived(EbmsUtils.toStringFromClasspath("soapenv-user-message.xml"));

        Exchange request = createRequest();
        context().createProducerTemplate().send("direct:testWireTap",request);

        assertMockEndpointsSatisfied();
        assertEventNotification(mockNotification.getExchanges().get(0));

    }

    @Override
    protected RouteBuilder [] createRouteBuilders() throws Exception {
        System.setProperty("jentrataVersion","TEST");
        EventNotificationRouteBuilder routeBuilder = new EventNotificationRouteBuilder();
        routeBuilder.setNotificationEndpoint(mockNotification.getEndpointUri());
        return new RouteBuilder[] {
                routeBuilder,
                new RouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        from("direct:testWireTap")
                            .wireTap(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
                            .to(mockOther);
                    }
                }
        };
    }

    private Exchange createRequest() throws Exception {
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.MESSAGE_FROM,"123456789");
        request.getIn().setHeader(EbmsConstants.MESSAGE_TO,"192837465");
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"orders123@buyer.jentrata.org");
        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_INBOUND);
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");
        request.getIn().setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,"testConvId");
        request.getIn().setHeader(EbmsConstants.MESSAGE_STATUS, MessageStatusType.RECEIVED);
        request.getIn().setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION,"All Good");
        request.getIn().setHeader(EbmsConstants.MESSAGE_DATE,"2013-06-12T10:45:00.000Z");

        request.getIn().setBody(EbmsUtils.toStringFromClasspath("soapenv-user-message.xml"));

        return request;

    }

    private Map<String,Object> assertEventNotification(Exchange event) throws IOException {
        assertThat(event.getIn().getBody(),notNullValue());
        System.out.println(event.getIn().getBody());
        Map<String,Object> message = fromJson(event.getIn().getBody(String.class));
        assertThat(message,notNullValue());
        assertThat((String) message.get("messageId"),equalTo("orders123@buyer.jentrata.org"));
        assertThat((String) message.get("direction"),equalTo(EbmsConstants.MESSAGE_DIRECTION_INBOUND));
        assertThat((String) message.get("cpaId"),equalTo("testCPAId"));
        assertThat((String) message.get("refMessageId"),equalTo(""));
        assertThat((String) message.get("conversationId"),equalTo("testConvId"));
        assertThat((String) message.get("status"),equalTo("RECEIVED"));
        assertThat((String) message.get("statusDescription"),equalTo("All Good"));
        assertThat((String) message.get("messageDate"),equalTo("2013-06-12T10:45:00.000Z"));
        Map<String,Object> headers = (Map<String, Object>) message.get("headers");
        assertThat((String) headers.get("JentrataFrom"),equalTo("123456789"));
        assertThat((String) headers.get("JentrataTo"),equalTo("192837465"));
        assertThat((String) headers.get("jentrataVersion"),equalTo("TEST"));
        return message;
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,Map.class);
    }
}
