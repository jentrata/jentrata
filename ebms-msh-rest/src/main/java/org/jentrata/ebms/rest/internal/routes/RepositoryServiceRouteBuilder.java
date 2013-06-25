package org.jentrata.ebms.rest.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.MessageStore;

import javax.ws.rs.core.MediaType;

/**
 * Provides the implementation of the RepositoryService
 *
 * @author aaronwalker
 */
public class RepositoryServiceRouteBuilder extends RouteBuilder {

    private MessageStore messageStore;

    @Override
    public void configure() throws Exception {
        from("direct:repository-findMessageById")
            .setHeader(EbmsConstants.MESSAGE_ID, simple("${body[0]}"))
            .setHeader(EbmsConstants.MESSAGE_DIRECTION, simple("${body[1]}"))
            .bean(messageStore, "findByMessageId")
            .setHeader(EbmsConstants.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
        .routeId("_jentrataRestRepositoryService-findMessageById");

        from("direct:repository-findPayloadById")
            .setHeader(EbmsConstants.MESSAGE_ID,body())
            .bean(messageStore, "findPayloadById")
            .setHeader(EbmsConstants.CONTENT_TYPE,constant(MediaType.APPLICATION_OCTET_STREAM))
        .routeId("_jentrataRestRepositoryService-findPayloadById");
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }
}
