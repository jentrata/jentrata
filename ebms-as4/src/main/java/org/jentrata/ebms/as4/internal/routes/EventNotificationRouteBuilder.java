package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.LoggingLevel;
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
            .onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.WARN,"unable to send event notification: ${exception.message}")
                .log(LoggingLevel.DEBUG,"${exception.stacktrace}")
            .end()
            .setProperty(EbmsConstants.JENTRATA_VERSION, simple("${sys.jentrataVersion}"))
            .choice()
                .when(header(EbmsConstants.MESSAGE_ID).isNotNull())
                    .to("freemarker:templates/eventNotification.ftl")
                    .removeHeaders("*")
                    .inOnly(notificationEndpoint)
                .otherwise()
                    .log(LoggingLevel.WARN,"uanble to send event notification messageId is null:\n${headers}")
                    .log(LoggingLevel.DEBUG,"Request:${body}")
        .routeId("_jentrataSendEventNotification");
    }

    public String getNotificationEndpoint() {
        return notificationEndpoint;
    }

    public void setNotificationEndpoint(String notificationEndpoint) {
        this.notificationEndpoint = notificationEndpoint;
    }
}
