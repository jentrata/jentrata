package org.jentrata.ebms.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.support.ExpressionAdapter;
import org.jentrata.ebms.EbmsConstants;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A splitter that takes a list of attachments on a message and splits them out
 * into individual messages with attachment being set as the body on the new
 * message. The original message body is available as message header called
 * CamelOriginalBody
 *
 * @author aaronwalker
 */
public class SplitAttachmentsToBody extends ExpressionAdapter {

    public static final String ORIGINAL_MESSAGE_BODY = "CamelOriginalBody";

    private final boolean copyOriginalMessage;
    private final boolean copyHeaders;
    private boolean includeOrignalMessage = false;

    public SplitAttachmentsToBody(){
        this(true,true,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage) {
        this(copyOriginalMessage,true,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage, boolean copyHeaders) {
        this(copyOriginalMessage,copyOriginalMessage,false);
    }

    public SplitAttachmentsToBody(boolean copyOriginalMessage, boolean copyHeaders, boolean includeOrignalMessage) {
        this.copyOriginalMessage = copyOriginalMessage;
        this.copyHeaders = copyHeaders;
        this.includeOrignalMessage = includeOrignalMessage;
    }



    @Override
    public Object evaluate(Exchange exchange) {

        String originalBody = exchange.getIn().getBody(String.class);

        // must use getAttachments to ensure attachments is initial populated
        if (!includeOrignalMessage && exchange.getIn().getAttachments().isEmpty()) {
            return null;
        }

        // we want to provide a list of messages with 1 attachment per mail
        List<Message> answer = new ArrayList<Message>();

        if(includeOrignalMessage && originalBody != null && originalBody.length() > 0) {

            answer.add(createNewMessage(EbmsConstants.SOAP_BODY_PAYLOAD_ID,
                    EbmsConstants.TEXT_XML_CONTENT_TYPE,
                    exchange.getIn().getBody(InputStream.class),
                    exchange));
        }

        for (Map.Entry<String, DataHandler> entry : exchange.getIn().getAttachments().entrySet()) {
            try {
                Message copy = createNewMessage(entry.getKey(),
                        entry.getValue().getContentType(),
                        entry.getValue().getInputStream(),
                        exchange);
                if(copyOriginalMessage) {
                    copy.setHeader(ORIGINAL_MESSAGE_BODY,originalBody);
                }
                answer.add(copy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return answer;
    }

    private Message createNewMessage(String contentId, String contentType, InputStream stream, Exchange exchange) {

        final Message copy = exchange.getIn().copy();
        copy.getAttachments().clear();
        if(!copyHeaders) {
            copy.removeHeaders("*");
        }
        String originalMessageID = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class);
        String messageDirection = exchange.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION,String.class);
        String messageMEP = exchange.getIn().getHeader(EbmsConstants.EBMS_MESSAGE_MEP,String.class);
        String messageVersion = exchange.getIn().getHeader(EbmsConstants.EBMS_VERSION,String.class);
        String messageStatus = exchange.getIn().getHeader(EbmsConstants.MESSAGE_STATUS,String.class);
        String messageFrom = exchange.getIn().getHeader(EbmsConstants.MESSAGE_FROM,String.class);
        String messageTo = exchange.getIn().getHeader(EbmsConstants.MESSAGE_TO,String.class);

        // get the content and convert it to byte[]
        byte[] data = exchange.getContext().getTypeConverter().convertTo(byte[].class, stream);

        copy.setBody(data);

        copy.setHeader(EbmsConstants.CONTENT_ID, contentId);
        copy.setHeader(EbmsConstants.CONTENT_TYPE, contentType);
        copy.setHeader(EbmsConstants.MESSAGE_ID, originalMessageID);
        copy.setHeader(EbmsConstants.EBMS_MESSAGE_MEP, messageMEP);
        copy.setHeader(EbmsConstants.MESSAGE_DIRECTION, messageDirection);
        copy.setHeader(EbmsConstants.EBMS_VERSION, messageVersion);
        copy.setHeader(EbmsConstants.MESSAGE_STATUS, messageStatus);
        copy.setHeader(EbmsConstants.MESSAGE_FROM, messageFrom);
        copy.setHeader(EbmsConstants.MESSAGE_TO, messageTo);

        return copy;
    }
}
