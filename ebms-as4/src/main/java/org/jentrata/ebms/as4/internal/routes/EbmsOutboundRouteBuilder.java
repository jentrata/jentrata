package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.messaging.MessageStore;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Setups and outbound route per trading partner
 *
 * @author aaronwalker
 */
public class EbmsOutboundRouteBuilder extends RouteBuilder {

    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String messageUpdateEndpoint = MessageStore.DEFAULT_MESSAGE_UPDATE_ENDPOINT;
    private CPARepository cpaRepository;
    private String httpProxyHost = null;
    private String httpProxyPort = null;
    private String httpClientOverride = null;

    @Override
    public void configure() throws Exception {
        from(outboundEbmsQueue)
            .removeHeaders("Camel*")
            .removeHeaders("JMS*")
            .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.DELIVER))
            .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, constant(null))
            .to(messageUpdateEndpoint)
            .recipientList(simple("direct:outbox_${headers.JentrataCPAId}"))
        .routeId("_jentrataEbmsOutbound");

        for(PartnerAgreement agreement : cpaRepository.getActivePartnerAgreements()) {
            from("direct:outbox_" + agreement.getCpaId())
                .log(LoggingLevel.INFO,"Delivering message to cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessageId}")
                .onException(Exception.class)
                    .handled(true)
                    .log(LoggingLevel.WARN,"Failed to send cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessgeId}: ${exception.message}")
                    .to("direct:processFailure")
                .end()
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to(configureEndpoint(agreement.getTransportReceiverEndpoint()))
                .choice()
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                        .to("direct:processSuccess")
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(204))
                        .to("direct:processSuccess")
                    .otherwise()
                        .to("direct:processFailure")
            .routeId("_jentrataEbmsOutbound" + agreement.getCpaId());
        }

        from("direct:processSuccess")
            .log(LoggingLevel.INFO,"Successfully delivered cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessageId} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG, "responseCode:${headers.CamelHttpResponseCode}\n${body}")
            .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.DELIVERED))
            .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, constant(null))
            .to(messageUpdateEndpoint)
            .wireTap(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
        .routeId("_jentrataEbmsOutboundSuccess");

        from("direct:processFailure")
            .log(LoggingLevel.ERROR, "Failed to deliver cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessgeId} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG,"responseCode:${headers.CamelHttpResponseCode}\n${body}")
            .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.FAILED))
            .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, simple("${headers.CamelHttpResponseCode} - ${body}"))
            .to(messageUpdateEndpoint)
            .wireTap(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
        .routeId("_jentrataEbmsOutboundFailure");

    }

    public String getOutboundEbmsQueue() {
        return outboundEbmsQueue;
    }

    public void setOutboundEbmsQueue(String outboundEbmsQueue) {
        this.outboundEbmsQueue = outboundEbmsQueue;
    }

    public String getMessageUpdateEndpoint() {
        return messageUpdateEndpoint;
    }

    public void setMessageUpdateEndpoint(String messageUpdateEndpoint) {
        this.messageUpdateEndpoint = messageUpdateEndpoint;
    }

    public CPARepository getCpaRepository() {
        return cpaRepository;
    }

    public void setCpaRepository(CPARepository cpaRepository) {
        this.cpaRepository = cpaRepository;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public String getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(String httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

    public String getHttpClientOverride() {
        return httpClientOverride;
    }

    public void setHttpClientOverride(String httpClientOverride) {
        this.httpClientOverride = httpClientOverride;
    }

    private String configureEndpoint(String endpoint) {
        if(endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint + "?" + configureOptions();
        } else {
            return endpoint;
        }
    }

    protected String configureOptions() {
        StringBuilder options = new StringBuilder();
        options.append("throwExceptionOnFailure=false");
        if(isNotEmpty(httpClientOverride)) {
            options.append("&" + httpClientOverride);
        } else {
            if(isNotEmpty(httpProxyHost) && isNotEmpty(httpProxyPort)) {
                options.append("&proxyHost=" + httpProxyHost);
                options.append("&proxyPort=" + httpProxyPort);
            }
        }
        return options.toString();
    }

    private static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }
}
