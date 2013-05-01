package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.MessageStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pickup outbound messages generates the ebMS envelope
 *
 * @author aaronwalker
 */
public class EbmsOutboundMessageRouteBuilder extends RouteBuilder {

    private String deliveryQueue = "activemq:queue:jentrata_as4_outbound";
    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private String messgeStoreEndpoint = MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT;

    @Override
    public void configure() throws Exception {

        from(deliveryQueue)
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    String body = exchange.getIn().getBody(String.class);
                    String contentType = exchange.getIn().getHeader(EbmsConstants.CONTENT_TYPE,String.class);
                    String contentCharset = exchange.getIn().getHeader(EbmsConstants.CONTENT_CHAR_SET,"UTF-8",String.class);
                    String payloadId = exchange.getIn().getHeader(EbmsConstants.PAYLOAD_ID,String.class);

                    List<Map<String,Object>> payloads = new ArrayList<>();
                    Map<String,Object> payloadMap = new HashMap<>();
                    payloadMap.put("payloadId",payloadId);
                    payloadMap.put("contentType",contentType);
                    payloadMap.put("charset",contentCharset);
                    payloadMap.put("content",body);
                    payloads.add(payloadMap);
                    exchange.getIn().setBody(payloads);
                }
            })
            .setHeader(EbmsConstants.MESSAGE_ID, simple("${bean:uuidGenerator.generateId}"))
            .setHeader(EbmsConstants.MESSAGE_TYPE, constant(MessageType.USER_MESSAGE))
            .setHeader(EbmsConstants.MESSAGE_DIRECTION, constant(EbmsConstants.MESSAGE_DIRECTION_OUTBOUND))
            .to("freemarker:templates/mimeMessage.ftl")
            .setHeader(EbmsConstants.CONTENT_TYPE,constant("Multipart/Related; boundary=\"----=_Part_7_10584188.1123489648993\"; type=\"application/soap+xml\"; start=\"<soapPart@jentrata.org>\""))
            .to(messgeStoreEndpoint)
            .to(outboundEbmsQueue)
        .routeId("_jentrataEbmsGenerateMessage");

    }

    public String getDeliveryQueue() {
        return deliveryQueue;
    }

    public void setDeliveryQueue(String deliveryQueue) {
        this.deliveryQueue = deliveryQueue;
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
}
