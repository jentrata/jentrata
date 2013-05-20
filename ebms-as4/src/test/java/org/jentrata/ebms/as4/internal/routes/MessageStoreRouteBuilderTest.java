package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.DefaultMessage;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Unit tests for org.jentrata.ebms.as4.internal.routes.MessageStoreRouteBuilder
 *
 * @author aaronwalker
 */
public class MessageStoreRouteBuilderTest extends CamelTestSupport {

    private DummyMessageStore messageStore;

    @Test
    public void testMessageStore() throws IOException {
        Exchange request = new DefaultExchange(context());
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        Exchange response = context().createProducerTemplate().send(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT,request);

        String msgId = response.getIn().getHeader(MessageStore.JENTRATA_MESSAGE_ID, String.class);
        assertThat(msgId,equalTo(request.getIn().getMessageId()));
        assertThat(messageStore.findByMessageId(msgId).getMessageId(),equalTo(msgId));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        MessageStoreRouteBuilder routeBuilder = new MessageStoreRouteBuilder();
        messageStore = new DummyMessageStore();
        routeBuilder.setMessageStore(messageStore);
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
        public Message findByMessageId(final String messageId) {
            return new DefaultMessage(messageId);
        }

        @Override
        public InputStream findPayloadById(String messageId) {
            return messageStore.get(messageId);
        }
    }
}
