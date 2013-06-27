package org.jentrata.ebms.soap;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.utils.EbmsUtils;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Marshals/Unmarshals org.wc3.Document into a SOAPMessage
 * Also handles messages with attachments
 *
 * @author aaronwalker
 */
@SuppressWarnings("unchecked")
public class SoapMessageDataFormat implements DataFormat {

    /**
     *  Converts a org.wc3.Document SOAPMessage
     *
     * @param exchange
     * @param graph
     * @param stream
     * @throws Exception
     */
    @Override
    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        String soapVersion = exchange.getIn().getHeader(EbmsConstants.SOAP_VERSION,SOAPConstants.SOAP_1_2_PROTOCOL,String.class);
        SOAPMessage soapMessage = EbmsUtils.parse(soapVersion,EbmsConstants.SOAP_XML_CONTENT_TYPE,exchange.getIn().getBody(InputStream.class));
        soapMessage.getSOAPPart().addMimeHeader(EbmsConstants.CONTENT_ID,"<soapPart@jentrata.org>");
        List<Map<String,Object>> payloads = (List<Map<String, Object>>) exchange.getIn().getHeader(EbmsConstants.MESSAGE_PAYLOADS);
        for(Map<String,Object> payload : payloads) {
            String payloadId = (String) payload.get("payloadId");
            String contentType = (String) payload.get("contentType");
            String compressionType = (String) payload.get("compressionType");
            byte [] content = (byte[]) payload.get("content");
            if(EbmsConstants.GZIP.equalsIgnoreCase(compressionType)) {
                contentType = compressionType;
            }
            EbmsUtils.addAttachment(soapMessage,payloadId,contentType,content);
        }
        exchange.getOut().setHeader(EbmsConstants.CONTENT_TYPE,soapMessage.getMimeHeaders().getHeader(EbmsConstants.CONTENT_TYPE)[0]);
        soapMessage.writeTo(stream);
    }

    /**
     * Converts a SOAPMessage into a org.wc3.Document
     *
     * Using a custom message header you can which SOAP Version the message is in
     *
     * @param exchange
     * @param stream
     * @return
     * @throws Exception
     */
    @Override
    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        InputStream body = exchange.getContext().getTypeConverter().convertTo(InputStream.class,exchange.getIn().getBody());
        String soapVersion = exchange.getIn().getHeader(EbmsConstants.SOAP_VERSION, SOAPConstants.SOAP_1_2_PROTOCOL, String.class);
        MessageFactory messageFactory = MessageFactory.newInstance(soapVersion);
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader(Exchange.CONTENT_TYPE, exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class));
        SOAPMessage message = messageFactory.createMessage(mimeHeaders, body);
        SOAPHeader soapHeader = message.getSOAPPart().getEnvelope().getHeader();

        if(message.countAttachments() > 0) {
            addAttachments(message, exchange);
        }

        return soapHeader.getOwnerDocument();
    }

    private void addAttachments(SOAPMessage message, Exchange exchange) throws SOAPException {
        Iterator<AttachmentPart> attachments = message.getAttachments();
        while (attachments.hasNext()) {
            AttachmentPart attachment = attachments.next();
            exchange.getOut().addAttachment(attachment.getContentId(),attachment.getDataHandler());
        }
    }
}
