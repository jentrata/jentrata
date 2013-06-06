package org.jentrata.ebms.rest.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.DefaultMessage;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Unit test for org.jentrata.ebms.rest.internal.routes.RepositoryServiceRouteBuilder
 *
 * @author aaronwalker
 */
public class RepositoryServiceRouteBuilderTest extends CamelTestSupport {

    @Test
    public void testFindByMessageID() {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody("testMessageID");
        Exchange response = context().createProducerTemplate().send("direct:repository-findMessageById",request);
        assertThat(response.getIn().getBody(Message.class).getMessageId(),equalTo("testMessageID"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        RepositoryServiceRouteBuilder routeBuilder = new RepositoryServiceRouteBuilder();
        routeBuilder.setMessageStore(new DummyMessageStore());
        return routeBuilder;
    }

    private static class DummyMessageStore implements MessageStore {

        private Map<String, InputStream> messageStore = new LinkedHashMap<>();

        @Override
        public void store(InputStream input, Exchange exchange) {
            messageStore.put(exchange.getIn().getMessageId(),input);
            exchange.getIn().setHeader(MESSAGE_STORE_REF,exchange.getIn().getMessageId());
            exchange.getIn().setHeader(JENTRATA_MESSAGE_ID,exchange.getIn().getMessageId());
        }

        @Override
        public void storeMessage(Exchange exchange) {}

        @Override
        public void updateMessage(String messageId,String messageDirection,MessageStatusType status,String statusDescription) {}

        @Override
        public Message findByMessageId(final String messageId, String messageDirection) {
            return new DefaultMessage(messageId);
        }

        @Override
        public InputStream findPayloadById(String messageId) {
            return messageStore.get(messageId);
        }
    }
}
