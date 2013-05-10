package org.jentrata.ebms.soap;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.SplitAttachmentsToBody;

/**
 * Allows
 *
 * @author aaronwalker
 */
public interface SoapPayloadProcessor {

    void process(@Header(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY)String soapMessage,
                 @Header(EbmsConstants.CONTENT_ID)String payloadId,
                 Exchange exchange);
}
