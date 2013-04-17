package org.jentrata.ebms.soap;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.jentrata.ebms.EbmsConstants;

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

/**
 * Marshals/Unmarshals org.wc3.Document into a SOAPMessage
 * Also handles messages with attachments
 *
 * @author aaronwalker
 */
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
        throw new UnsupportedOperationException("marshal operation not supported for this dataformat");
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
        String soapVersion = exchange.getIn().getHeader(EbmsConstants.SOAP_VERSION, SOAPConstants.SOAP_1_1_CONTENT_TYPE, String.class);
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
