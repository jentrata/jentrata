package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;

/**
 * Generates and Ebms Signal Message based on a given soap message
 *
 * @author aaronwalker
 */
public class EbmsSignalMessageRouteBuilder extends RouteBuilder {

    private String inboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_inbound";
    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";

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
