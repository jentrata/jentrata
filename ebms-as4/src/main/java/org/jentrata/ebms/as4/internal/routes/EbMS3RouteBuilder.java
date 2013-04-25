package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.freemarker.FreemarkerConstants;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.internal.messaging.MessageDetector;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.soap.SoapMessageDataFormat;

/**
 * Exposes an HTTP endpoint that consumes AS4 Messages
 *
 * @author aaronwalker
 */
public class EbMS3RouteBuilder extends RouteBuilder {

    private String ebmsHttpEndpoint = "jetty:http://0.0.0.0:8081/jentrata/ebms/inbound";
    private String inboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_inbound";
    private String messgeStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;
    private String validateTradingPartner = "direct:validatePartner";

    private MessageDetector messageDetector;

    @Override
    public void configure() throws Exception {

        from(ebmsHttpEndpoint)
            .streamCaching()
            .onException(UnsupportedOperationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(405))
                .setHeader("Allow",constant("POST"))
                .to("direct:errorHandler")
            .end()
            .onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.DEBUG, "headers:${headers}\nbody:\n${in.body}")
                .log(LoggingLevel.ERROR, "${exception.message}\n${exception.stacktrace}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .to("direct:errorHandler")
             .end()
            .log(LoggingLevel.INFO, "Request:${headers}")
            .setHeader(EbmsConstants.MESSAGE_DIRECTION,constant(EbmsConstants.MESSAGE_DIRECTION_INBOUND))
            .choice()
                .when(header(Exchange.HTTP_METHOD).isNotEqualTo("POST"))
                    .throwException(new UnsupportedOperationException("Http Method Not Allowed"))
             .end()
            .bean(messageDetector, "parse") //Determine what type of message it is for example SOAP 1.1 or SOAP 1.2 ebms2 or ebms3 etc
            .to(messgeStoreEndpoint) //essentially we claim check the raw incoming message/payload
            .unmarshal(new SoapMessageDataFormat()) //extract the SOAP Envelope as set it has the message body
            .convertBodyTo(String.class)
            .choice()
                .when(header(EbmsConstants.EBMS_VERSION).isEqualTo(EbmsConstants.EBMS_V3))
                    .to(validateTradingPartner)
                    .inOnly(inboundEbmsQueue)
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                    .setBody(constant(null))
                    .to("direct:removeHeaders")
                .otherwise()
                    .throwException(new UnsupportedOperationException("currently only ebMS V3.0 is supported by Jentrata"))
        .routeId("_jentrataEbmsInbound");

        from("direct:errorHandler")
            .to("direct:removeHeaders")
            .setHeader("CamelException",simple("${exception.message}"))
            .setHeader("CamelExceptionStackTrace",simple("${exception.stacktrace}"))
            .setHeader(FreemarkerConstants.FREEMARKER_RESOURCE_URI,simple("html/${headers.CamelHttpResponseCode}.html"))
            .to("freemarker:html/500.html")
        .end()
        .routeId("_jentrataErrorHandler");

        from("direct:removeHeaders")
            .removeHeaders("Jentrata*")
            .removeHeaders("Accept*")
            .removeHeaders("Content*")
            .removeHeader("Host")
            .removeHeader("User-Agent")
            .removeHeader("breadcrumbId")
        .routeId("_jentrataRemoveHeaders");
    }

    public String getEbmsHttpEndpoint() {
        return ebmsHttpEndpoint;
    }

    public void setEbmsHttpEndpoint(String ebmsHttpEndpoint) {
        this.ebmsHttpEndpoint = ebmsHttpEndpoint;
    }

    public String getInboundEbmsQueue() {
        return inboundEbmsQueue;
    }

    public void setInboundEbmsQueue(String inboundEbmsQueue) {
        this.inboundEbmsQueue = inboundEbmsQueue;
    }

    public String getMessgeStoreEndpoint() {
        return messgeStoreEndpoint;
    }

    public void setMessgeStoreEndpoint(String messgeStoreEndpoint) {
        this.messgeStoreEndpoint = messgeStoreEndpoint;
    }

    public String getValidateTradingPartner() {
        return validateTradingPartner;
    }

    public void setValidateTradingPartner(String validateTradingPartner) {
        this.validateTradingPartner = validateTradingPartner;
    }

    public MessageDetector getMessageDetector() {
        return messageDetector;
    }

    public void setMessageDetector(MessageDetector messageDetector) {
        this.messageDetector = messageDetector;
    }
}
