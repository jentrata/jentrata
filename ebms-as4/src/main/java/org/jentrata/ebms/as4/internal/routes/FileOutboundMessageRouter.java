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
    private String messageId;
    private String cpaId;
    private String service;
    private String action;
    private String contentType;
    private String from;
    private String to;
    private String fromPartyIdType;
    private String toPartyIdType;
    private String payloadId;
    private String agreementRef;
    private String schema;
    private String partProperties;
    private String conversationId = "${bean:uuidGenerator.generateId}";
    private String deliveryQueue = "activemq:queue:jentrata_as4_outbound";

    @Override
    public void configure() throws Exception {

        String convId =  "${bean:uuidGenerator.generateId}";
        if(conversationId != null && conversationId.length() > 0) {
            convId = conversationId;
        }

        String msgID = "${bean:uuidGenerator.generateId}";
        if(messageId != null && !messageId.isEmpty()) {
            msgID = messageId;
        }

        from(fileEndpoint)
            .setHeader(EbmsConstants.MESSAGE_ID,simple(msgID))
            .setHeader(EbmsConstants.CPA_ID, constant(cpaId))
            .setHeader(EbmsConstants.MESSAGE_SERVICE, constant(service))
            .setHeader(EbmsConstants.MESSAGE_ACTION, constant(action))
            .setHeader(EbmsConstants.CONTENT_TYPE, constant(contentType))
            .setHeader(EbmsConstants.MESSAGE_FROM,constant(from))
            .setHeader(EbmsConstants.MESSAGE_FROM_TYPE, constant(fromPartyIdType))
            .setHeader(EbmsConstants.MESSAGE_TO, constant(to))
            .setHeader(EbmsConstants.MESSAGE_TO_TYPE, constant(toPartyIdType))
            .setHeader(EbmsConstants.PAYLOAD_ID, constant(payloadId))
            .setHeader(EbmsConstants.MESSAGE_CONVERSATION_ID, simple(convId))
            .setHeader(EbmsConstants.MESSAGE_AGREEMENT_REF,constant(agreementRef))
            .setHeader(EbmsConstants.MESSAGE_PAYLOAD_SCHEMA,constant(schema))
            .setHeader(EbmsConstants.MESSAGE_PART_PROPERTIES,constant(partProperties))
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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getFromPartyIdType() {
        return fromPartyIdType;
    }

    public void setFromPartyIdType(String fromPartyIdType) {
        this.fromPartyIdType = fromPartyIdType;
    }

    public String getToPartyIdType() {
        return toPartyIdType;
    }

    public void setToPartyIdType(String toPartyIdType) {
        this.toPartyIdType = toPartyIdType;
    }

    public String getAgreementRef() {
        return agreementRef;
    }

    public void setAgreementRef(String agreementRef) {
        this.agreementRef = agreementRef;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPartProperties() {
        return partProperties;
    }

    public void setPartProperties(String partProperties) {
        this.partProperties = partProperties;
    }

    public String getDeliveryQueue() {
        return deliveryQueue;
    }

    public void setDeliveryQueue(String deliveryQueue) {
        this.deliveryQueue = deliveryQueue;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
