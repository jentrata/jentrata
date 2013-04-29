package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.MessageStore;

/**
 * Generates and Ebms Signal Message based on a given soap message
 *
 * @author aaronwalker
 */
public class EbmsSignalMessageRouteBuilder extends RouteBuilder {

    private String inboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_inbound";
    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String messgeStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;

    @Override
    public void configure() throws Exception {
        from(inboundEbmsQueue)
            .convertBodyTo(String.class)
            .to("direct:receiptRequired")
            .choice()
                .when(header(EbmsConstants.EBMS_RECEIPT_REQUIRED).isEqualTo(true))
                    .log(LoggingLevel.INFO, "Generating receipt message for from:${headers.jentrataFrom} - to:${headers.jentrataTo} - ${headers.jentrataMessageId}")
                    .setHeader("messageid", simple("${bean:uuidGenerator.generateId}"))
                    .setHeader("timestamp",simple("${date:now:yyyy-MM-dd'T'HH:mm:ss.S'Z'}"))
                    .to("xslt:templates/signalMessage.xsl")
                    .convertBodyTo(String.class)
                    .setHeader(EbmsConstants.MESSAGE_DIRECTION, constant(EbmsConstants.MESSAGE_DIRECTION_OUTBOUND))
                    .setHeader(EbmsConstants.CONTENT_TYPE,constant(EbmsConstants.SOAP_XML_CONTENT_TYPE))
                    .setHeader(EbmsConstants.MESSAGE_ID,header("messageid"))
                    .setHeader(EbmsConstants.MESSAGE_TYPE,constant(MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE))
                    .to(messgeStoreEndpoint) //store the outbound signal message
                    .to(outboundEbmsQueue)
                .otherwise()
                    .log(LoggingLevel.INFO,"No receipt required for from:${headers.jentrataFrom} - to:${headers.jentrataTo} - ${headers.jentrataMessageId}")
        .routeId("_jentrataEbmsGenerateSignalMessage");

        from("direct:receiptRequired")
            .setHeader(EbmsConstants.EBMS_RECEIPT_REQUIRED,constant("true"))
        .routeId("_jentrataEbmsReceiptRequired");
    }

    public String getInboundEbmsQueue() {
        return inboundEbmsQueue;
    }

    public void setInboundEbmsQueue(String inboundEbmsQueue) {
        this.inboundEbmsQueue = inboundEbmsQueue;
    }

    public String getOutboundEbmsQueue() {
        return outboundEbmsQueue;
    }

    public void setOutboundEbmsQueue(String outboundEbmsQueue) {
        this.outboundEbmsQueue = outboundEbmsQueue;
    }
}
