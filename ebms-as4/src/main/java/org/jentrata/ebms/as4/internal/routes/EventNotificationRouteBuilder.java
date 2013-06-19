package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;

/**
 * Publishes event notifications
 *
 * @author aaronwalker
 */
public class EventNotificationRouteBuilder extends RouteBuilder {

    public static final String SEND_NOTIFICATION_ENDPOINT = "direct:sendNotification";

    private String notificationEndpoint = "activemq:topic:VirtualTopic.jentrata_event_notification";

    @Override
    public void configure() throws Exception {
        from(SEND_NOTIFICATION_ENDPOINT)
            .setProperty(EbmsConstants.JENTRATA_VERSION,simple("${sys.jentrataVersion}"))
            .to("freemarker:templates/eventNotification.ftl")
            .removeHeaders("*")
            .inOnly(notificationEndpoint)
        .routeId("_jentrataSendEventNotification");
    }

    public String getNotificationEndpoint() {
        return notificationEndpoint;
    }

    public void setNotificationEndpoint(String notificationEndpoint) {
        this.notificationEndpoint = notificationEndpoint;
    }
}
