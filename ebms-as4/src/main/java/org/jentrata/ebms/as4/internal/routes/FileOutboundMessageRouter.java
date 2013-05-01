package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;

/**
 * Pickup a file from a given directory and deliveries it as an AS4 message
 *
 * @author aaronwalker
 */
public class FileOutboundMessageRouter extends RouteBuilder {

    private String fileEndpoint;
    private String cpaId;
    private String contentType;
    private String from;
    private String to;
    private String payloadId;
    private String conversationId = "${bean:uuidGenerator.generateId}";
    private String deliveryQueue = "activemq:queue:jentrata_as4_outbound";


    @Override
    public void configure() throws Exception {
        from(fileEndpoint)
            .setHeader(EbmsConstants.CPA_ID,constant(cpaId))
            .setHeader(EbmsConstants.CONTENT_TYPE,constant(contentType))
            .setHeader(EbmsConstants.MESSAGE_FROM,constant(from))
            .setHeader(EbmsConstants.MESSAGE_TO,constant(to))
            .setHeader(EbmsConstants.PAYLOAD_ID,constant(payloadId))
            .setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,simple(conversationId))
            .to(deliveryQueue)
        .routeId("_jentrataFileOutbound");
    }

    public String getFileEndpoint() {
        return fileEndpoint;
    }

    public void setFileEndpoint(String fileEndpoint) {
        this.fileEndpoint = fileEndpoint;
    }

    public String getCpaId() {
        return cpaId;
    }

    public void setCpaId(String cpaId) {
        this.cpaId = cpaId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getDeliveryQueue() {
        return deliveryQueue;
    }

    public void setDeliveryQueue(String deliveryQueue) {
        this.deliveryQueue = deliveryQueue;
    }
}
