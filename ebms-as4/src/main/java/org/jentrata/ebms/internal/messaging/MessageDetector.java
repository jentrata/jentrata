package org.jentrata.ebms.internal.messaging;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;

import javax.xml.soap.SOAPConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Determines ebxml message types and versions and set header attributes accordingly
 *
 * @author aaronwalker
 */
public class MessageDetector {

    private Pattern messageIdRegex = Pattern.compile("<(.*?)MessageId>(.*?)</(.*?)MessageId>", Pattern.DOTALL | Pattern.MULTILINE);
    private Pattern refToMessageIdRegex = Pattern.compile("<(.*?)RefToMessageId>(.*?)</(.*?)RefToMessageId>", Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * Partially reads the input message and determines what type of message this is
     *
     * (right now this it pretty dumb the goal for this would to replace this with some sort of message codec)
     *
     * @param input  - the message input stream
     * @param headers- the message headers that we will add the additional headers to contain the message version details
     * @throws IOException
     */
    public void parse(@Body InputStream input, @Headers Map<String, Object> headers) throws IOException {
        try {
            byte [] msgData = new byte[4096];
            int count = input.read(msgData);
            if(count > 0) {
                String msg = new String(msgData); //should be able to use a header to determine encoding

                //First determine if the message is a SOAP 1.1 or 1.2 message by default we will assume 1.1
                String soapVersion = msg.contains(EbmsConstants.SOAP_1_2_NAMESPACE) ? SOAPConstants.SOAP_1_2_PROTOCOL : SOAPConstants.SOAP_1_1_PROTOCOL;
                headers.put(EbmsConstants.SOAP_VERSION,soapVersion);

                //next determine what version of ebms message is it, by default assume ebms V2
                String ebmsVersion = msg.contains(EbmsConstants.EBXML_V3_NAMESPACE) ? EbmsConstants.EBMS_V3 : EbmsConstants.EBMS_V2;
                headers.put(EbmsConstants.EBMS_VERSION,ebmsVersion);

                headers.put(EbmsConstants.MESSAGE_ID, getMessageId(msg));
                headers.put(EbmsConstants.REF_TO_MESSAGE_ID, getRefMessageId(msg));
                headers.put(EbmsConstants.MESSAGE_TYPE, getMessageType(msg));
            }
        } finally {
            input.reset();
        }
    }

    private String getRefMessageId(String msg) {
        Matcher matcher = refToMessageIdRegex.matcher(msg);
        if(matcher.find()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }

    private String getMessageId(String msg) {
        Matcher matcher = messageIdRegex.matcher(msg);
        if(matcher.find()) {
            return matcher.group(2);
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private MessageType getMessageType(String msg) {
        MessageType msgType = MessageType.UNKNOWN;
        if(msg.contains("UserMessage") && msg.contains("SignalMessage")) {
            msgType = MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE;
        } else if(msg.contains("UserMessage")) {
            msgType = MessageType.USER_MESSAGE;
        } else if(msg.contains("SignalMessage")) {
            if(msg.contains("Error")) {
                msgType = MessageType.SIGNAL_MESSAGE_ERROR;
            } else {
                msgType = MessageType.SIGNAL_MESSAGE;
            }
        }
        return msgType;
    }
}
