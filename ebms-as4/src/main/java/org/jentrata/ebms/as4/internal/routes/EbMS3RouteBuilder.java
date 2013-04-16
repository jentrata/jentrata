package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataFormat;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.MessageDetector;
import org.jentrata.ebms.messaging.MessageStore;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

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
            .onException(Exception.class)
                .log(LoggingLevel.INFO, "body:\n${in.body}")
                .log(LoggingLevel.ERROR, "${exception.message}\n${exception.stacktrace}")
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(exceptionMessage())
             .end()
            .log(LoggingLevel.INFO, "Request:${headers}")
            .bean(messageDetector, "parse") //Determine what type of message it is for example SOAP 1.1 or SOAP 1.2 ebms2 or ebms3 etc
            .to(messgeStoreEndpoint) //essentially we claim check the raw incoming message/payload
            .unmarshal(new DataFormat() {
                @Override
                public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
                    throw new UnsupportedOperationException("marshal operation not supported for this dataformat");
                }

                @Override
                public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
                    InputStream body = exchange.getContext().getTypeConverter().convertTo(InputStream.class,exchange.getIn().getBody());
                    String soapVersion = exchange.getIn().getHeader(EbmsConstants.SOAP_VERSION, SOAPConstants.SOAP_1_1_CONTENT_TYPE, String.class);
                    MessageFactory messageFactory = MessageFactory.newInstance(soapVersion);
                    MimeHeaders mimeHeaders = new MimeHeaders();
                    mimeHeaders.addHeader(Exchange.CONTENT_TYPE, exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class));
                    SOAPMessage message = messageFactory.createMessage(mimeHeaders, body);
                    SOAPHeader soapHeader = message.getSOAPPart().getEnvelope().getHeader();
                    return soapHeader.getOwnerDocument();
                }
            }) //convert this into a soap message
            .convertBodyTo(String.class)
            .choice()
                .when(header(EbmsConstants.EBMS_VERSION).isEqualTo(EbmsConstants.EBMS_V3))
                    .to(validateTradingPartner)
                    .inOnly(inboundEbmsQueue)
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                    .setBody(constant(null))
                .otherwise()
                    .throwException(new UnsupportedOperationException("currently only ebMS V3.0 is supported by Jentrata"))
        .routeId("_jentrataEbmsInbound");
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
