package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.MessageStore;

/**
 * Processing incoming ebms3 Signal Messages
 *
 * @author aaronwalker
 */
public class EbmsSignalMessageHandlerRouteBuilder extends RouteBuilder {

    private String inboundEbmsSignalsQueue = "activemq:queue:jentrata_internal_ebms_inbound_signals";
    private String messageUpdateEndpoint = MessageStore.DEFAULT_MESSAGE_UPDATE_ENDPOINT;

    @Override
    public void configure() throws Exception {

        final Namespaces ns = new Namespaces("S12", "http://www.w3.org/2003/05/soap-envelope")
                .add("eb3", "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/");

        from(inboundEbmsSignalsQueue)
            .choice()
                .when(header(EbmsConstants.CPA_ID).isEqualTo(EbmsConstants.CPA_ID_UNKNOWN))
                    .log(LoggingLevel.WARN,"Received SignalMessage from unknown partner - msgId:${headers.JentrataMessageID} - type:${headers.JentrataMessageType}")
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.SIGNAL_MESSAGE))
                    .log(LoggingLevel.INFO,"Received Signal Message for - cpaId:${headers.JentrataCPAId} - msgId:${headers.JentrataMessageID} - refMsgId:${headers.JentrataRefMessageID}")
                    .setHeader("JentrataOriginalMessageID",header(EbmsConstants.MESSAGE_ID))
                    .setHeader(EbmsConstants.MESSAGE_ID,header(EbmsConstants.REF_TO_MESSAGE_ID))
                    .setHeader(EbmsConstants.MESSAGE_STATUS, constant(MessageStatusType.DONE))
                    .to(messageUpdateEndpoint)
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.SIGNAL_MESSAGE_ERROR))
                    .setHeader("JentrataOriginalMessageID", header(EbmsConstants.MESSAGE_ID))
                    .setHeader(EbmsConstants.REF_TO_MESSAGE_ID, ns.xpath("//eb3:Error/@refToMessageInError",String.class))
                    .log(LoggingLevel.INFO, "Received Error Signal Message for - cpaId:${headers.JentrataCPAId} - msgId:${headers.JentrataMessageID} - refMsgId:${headers.JentrataRefMessageID}")
                    .setHeader(EbmsConstants.MESSAGE_ID, header(EbmsConstants.REF_TO_MESSAGE_ID))
                    .setHeader(EbmsConstants.MESSAGE_STATUS,constant(MessageStatusType.ERROR))
                    .setHeader(EbmsConstants.MESSAGE_STATUS_DESCRIPTION,xpath("//*[local-name()='Description'][1]/text()").stringResult())
                    .to(messageUpdateEndpoint)
                .otherwise()
                    .log(LoggingLevel.WARN,"Received unknown message type - msgId:${headers.JentrataMessageID} - type:${headers.JentrataMessageType}")
            .end()
        .routeId("_jentrataSignalMessageHandler");
    }

    public String getInboundEbmsSignalsQueue() {
        return inboundEbmsSignalsQueue;
    }

    public void setInboundEbmsSignalsQueue(String inboundEbmsSignalsQueue) {
        this.inboundEbmsSignalsQueue = inboundEbmsSignalsQueue;
    }

    public String getMessageUpdateEndpoint() {
        return messageUpdateEndpoint;
    }

    public void setMessageUpdateEndpoint(String messageUpdateEndpoint) {
        this.messageUpdateEndpoint = messageUpdateEndpoint;
    }
}
