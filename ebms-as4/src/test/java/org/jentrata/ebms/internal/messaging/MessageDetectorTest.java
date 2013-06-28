package org.jentrata.ebms.internal.messaging;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;

import javax.xml.soap.SOAPConstants;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

/**
 * Unit test for org.jentrata.ebms.internal.messaging.MessageDetector
 *
 * @author aaronwalker
 */
public class MessageDetectorTest  extends CamelTestSupport {

    @Test
    public void testSoap12SignMessage() throws Exception {

        MessageDetector messageDetector = new MessageDetector();
        Map<String,Object> headers = new HashMap<>();
        InputStream stream = new ByteArrayInputStream(EbmsUtils.toStringFromClasspath("signal-message.xml").getBytes());
        messageDetector.parse(stream,headers);

        assertThat((String) headers.get(EbmsConstants.SOAP_VERSION),equalTo(SOAPConstants.SOAP_1_2_PROTOCOL));
        assertThat((String) headers.get(EbmsConstants.EBMS_VERSION),equalTo(EbmsConstants.EBMS_V3));
        assertThat((String) headers.get(EbmsConstants.MESSAGE_ID),equalTo("006a655f0000013f7ee6da6400059448@qvalent.com"));
        assertThat((String) headers.get(EbmsConstants.REF_TO_MESSAGE_ID),equalTo("60c6be51-800b-4329-9430-180cb86c8295@jentrata.org"));
        assertThat((String) headers.get(EbmsConstants.MESSAGE_TYPE),equalTo(MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE.name()));

    }

    @Test
    public void testSoap12SignReceipt() throws Exception {

        MessageDetector messageDetector = new MessageDetector();
        Map<String,Object> headers = new HashMap<>();
        InputStream stream = new ByteArrayInputStream(EbmsUtils.toStringFromClasspath("signed-receipt.xml").getBytes());
        messageDetector.parse(stream,headers);

        assertThat((String) headers.get(EbmsConstants.SOAP_VERSION),equalTo(SOAPConstants.SOAP_1_2_PROTOCOL));
        assertThat((String) headers.get(EbmsConstants.EBMS_VERSION),equalTo(EbmsConstants.EBMS_V3));
        assertThat((String) headers.get(EbmsConstants.MESSAGE_ID),equalTo("006a655f0000013f88802ee700043563@qvalent.com"));
        assertThat((String) headers.get(EbmsConstants.REF_TO_MESSAGE_ID),equalTo("757c7afb-119a-4340-b143-e003bcef0a6c@jentrata.org"));
        assertThat((String) headers.get(EbmsConstants.MESSAGE_TYPE),equalTo(MessageType.SIGNAL_MESSAGE.name()));

    }
}
