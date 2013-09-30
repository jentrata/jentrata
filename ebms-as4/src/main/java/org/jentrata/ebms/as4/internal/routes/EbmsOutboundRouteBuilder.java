package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.messaging.MessageStore;

/**
 * Setups and outbound route per trading partner
 *
 * @author aaronwalker
 */
public class EbmsOutboundRouteBuilder extends RouteBuilder {

    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String ebmsResponseInbound = "activemq:queue:jentrata_internal_ebms_response";
    private String messageUpdateEndpoint = MessageStore.DEFAULT_MESSAGE_UPDATE_ENDPOINT;
    private CPARepository cpaRepository;
    private String httpProxyHost = null;
    private String httpProxyPort = null;
    private String httpClientOverride = null;

    @Override
    public void configure() throws Exception {
        from(outboundEbmsQueue)
            .log(LoggingLevel.DEBUG, "Outbound:${headers}\n${body}")
            .removeHeaders("Camel*")
            .removeHeaders("JMS*")
            .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.DELIVER))
            .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, constant(null))
            .to(messageUpdateEndpoint)
            .recipientList(simple("direct:outbox_${headers.JentrataCPAId}"))
        .routeId("_jentrataEbmsOutbound");

        for(PartnerAgreement agreement : cpaRepository.getActivePartnerAgreements()) {
            from("direct:outbox_" + agreement.getCpaId())
                .setProperty(EbmsConstants.CPA_ID,header(EbmsConstants.CPA_ID))
                .setProperty(EbmsConstants.MESSAGE_ID,header(EbmsConstants.MESSAGE_ID))
                .setProperty(EbmsConstants.MESSAGE_TYPE, header(EbmsConstants.MESSAGE_TYPE))
                .setProperty(EbmsConstants.MESSAGE_DIRECTION, header(EbmsConstants.MESSAGE_DIRECTION))
                .setProperty(EbmsConstants.MESSAGE_CONVERSATION_ID, header(EbmsConstants.MESSAGE_CONVERSATION_ID))
                .setProperty(EbmsConstants.MESSAGE_DATE, header(EbmsConstants.MESSAGE_DATE))
                .log(LoggingLevel.INFO, "Delivering message to cpaId:${property.JentrataCPAId} - type:${property.JentrataMessageType} - msgId:${property.JentrataMessageID}")
                .onException(Exception.class)
                    .handled(true)
                    .log(LoggingLevel.WARN, "Failed to send cpaId:${property.JentrataCPAId} - type:${property.JentrataMessageType} - msgId:${property.JentrataMessageID}: ${exception.message}")
                    .to("direct:processFailure")
                .end()
                .removeHeaders("Jentrata*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to(configureEndpoint(agreement.getProtocol().getAddress()))
                .setHeader(EbmsConstants.CPA_ID, property(EbmsConstants.CPA_ID))
                .setHeader(EbmsConstants.MESSAGE_ID, property(EbmsConstants.MESSAGE_ID))
                .setHeader(EbmsConstants.MESSAGE_TYPE, property(EbmsConstants.MESSAGE_TYPE))
                .setHeader(EbmsConstants.MESSAGE_DIRECTION, property(EbmsConstants.MESSAGE_DIRECTION))
                .setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID, property(EbmsConstants.MESSAGE_CONVERSATION_ID))
                .setHeader(EbmsConstants.MESSAGE_DATE, property(EbmsConstants.MESSAGE_DATE))
                .convertBodyTo(String.class)
                .choice()
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                        .to("direct:processSuccess")
                        .setProperty(EbmsConstants.CPA_ID,header(EbmsConstants.CPA_ID))
                        .setProperty(EbmsConstants.CONTENT_TYPE,header(Exchange.CONTENT_TYPE))
                        .removeHeaders("*")
                        .setHeader(EbmsConstants.CONTENT_TYPE, property(EbmsConstants.CONTENT_TYPE))
                        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                        .setHeader(EbmsConstants.CPA_ID, property(EbmsConstants.CPA_ID))
                        .inOnly(ebmsResponseInbound)
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(204))
                        .to("direct:processSuccess")
                    .otherwise()
                        .to("direct:processFailure")
            .routeId("_jentrataEbmsOutbound" + agreement.getCpaId());
        }

        from("direct:processSuccess")
            .log(LoggingLevel.INFO,"Successfully delivered cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessageID} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG, "responseCode:${headers.CamelHttpResponseCode}\nheaders:${headers}\n${body}")
            .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.DELIVERED))
            .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION, constant(null))
            .to(messageUpdateEndpoint)
            .wireTap(EventNotificationRouteBuilder.SEND_NOTIFICATION_ENDPOINT)
        .routeId("_jentrataEbmsOutboundSuccess");

        from("direct:processFailure")
            .log(LoggingLevel.ERROR, "Failed to deliver cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessageID} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG, "responseCode:${headers.CamelHttpResponseCode}\nheaders:${headers}\n${body}")
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

    public String getEbmsResponseInbound() {
        return ebmsResponseInbound;
    }

    public void setEbmsResponseInbound(String ebmsResponseInbound) {
        this.ebmsResponseInbound = ebmsResponseInbound;
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
            return endpoint + configureOptions(endpoint);
        } else {
            return endpoint;
        }
    }

    protected String configureOptions(String endpoint) {
        StringBuilder options = new StringBuilder();
        if(endpoint.contains("?")) {
            options.append("&");
        } else {
            options.append("?");
        }
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
