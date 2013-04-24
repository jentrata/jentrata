package org.jentrata.ebms.internal.messaging;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.jentrata.ebms.EbmsConstants;

import javax.xml.soap.SOAPConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Determines ebxml message types and versions and set header attributes accordingly
 *
 * @author aaronwalker
 */
public class MessageDetector {

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
            }
        } finally {
            input.reset();
        }
    }
}
