package org.jentrata.ebms.rest.internal.routes;

import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.MessageStore;

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
            .setHeader(EbmsConstants.MESSAGE_ID,body())
            .bean(messageStore, "findByMessageId")
        .routeId("_jentrataRestRepositoryService-findMessageById");
    }

    public MessageStore getMessageStore() {
        return messageStore;
    }

    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }
}
