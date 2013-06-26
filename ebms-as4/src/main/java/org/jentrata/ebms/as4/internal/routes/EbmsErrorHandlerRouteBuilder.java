package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.EbmsError;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.messaging.MessageStore;

/**
 * Generates error message in accordance with the ebMS 3.0 Core Spec
 *
 * @author aaronwalker
 */
public class EbmsErrorHandlerRouteBuilder extends RouteBuilder {

    private String errorQueue = "activemq:queue:jentrata_internal_ebms_error";
    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String messgeStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;
    private String messageInsertEndpoint = MessageStore.DEFAULT_MESSAGE_INSERT_ENDPOINT;

    @Override
    public void configure() throws Exception {
        from(errorQueue)
            .setHeader("JentrataRefToMessageInError",header(EbmsConstants.MESSAGE_ID))
            .setHeader(EbmsConstants.REF_TO_MESSAGE_ID,header(EbmsConstants.MESSAGE_ID))
            .setHeader(EbmsConstants.MESSAGE_ID, simple("${bean:uuidGenerator.generateId}"))
            .setHeader(EbmsConstants.MESSAGE_DIRECTION,constant(EbmsConstants.MESSAGE_DIRECTION_OUTBOUND))
            .setHeader(EbmsConstants.MESSAGE_TYPE,constant(MessageType.SIGNAL_MESSAGE_ERROR.name()))
            .setHeader(EbmsConstants.CONTENT_TYPE,constant(EbmsConstants.SOAP_XML_CONTENT_TYPE))
            .setHeader(EbmsConstants.MESSAGE_STATUS,constant(MessageStatusType.DELIVER))
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    String ebmsErrorCode = exchange.getIn().getHeader(EbmsConstants.EBMS_ERROR_CODE, EbmsError.EBMS_0004.getErrorCode(), String.class);
                    exchange.getIn().setHeader(EbmsConstants.EBMS_ERROR, EbmsError.getEbmsError(ebmsErrorCode));
                }
            })
            .to("freemarker:templates/errorMessage.ftl")
            .to(messgeStoreEndpoint)
            .to(messageInsertEndpoint)
            .to("direct:deliveryErrorReceipt")
        .routeId("_jentrataEbmsErrorHandler");

        from("direct:deliveryErrorReceipt")
            .choice()
                .when(header(EbmsConstants.MESSAGE_RECEIPT_PATTERN).isEqualTo(Security.ReplyPatternType.Response))
                    .log(LoggingLevel.DEBUG,"receipt returned on response")
                .otherwise()
                    .to(outboundEbmsQueue)
        .routeId("_jentrataEbmsDeliveryErrorReceipt");
    }

    public String getErrorQueue() {
        return errorQueue;
    }

    public void setErrorQueue(String errorQueue) {
        this.errorQueue = errorQueue;
    }

    public String getOutboundEbmsQueue() {
        return outboundEbmsQueue;
    }

    public void setOutboundEbmsQueue(String outboundEbmsQueue) {
        this.outboundEbmsQueue = outboundEbmsQueue;
    }

    public String getMessgeStoreEndpoint() {
        return messgeStoreEndpoint;
    }

    public void setMessgeStoreEndpoint(String messgeStoreEndpoint) {
        this.messgeStoreEndpoint = messgeStoreEndpoint;
    }

    public String getMessageInsertEndpoint() {
        return messageInsertEndpoint;
    }

    public void setMessageInsertEndpoint(String messageInsertEndpoint) {
        this.messageInsertEndpoint = messageInsertEndpoint;
    }
}
