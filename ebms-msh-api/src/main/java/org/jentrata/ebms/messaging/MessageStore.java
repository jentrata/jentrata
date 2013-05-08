package org.jentrata.ebms.messaging;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;

import java.io.InputStream;

/**
 *  A ebMS MessageStore interface
 *
 * @author aaronwalker
 */
public interface MessageStore {

    public static final String DEFAULT_MESSAGE_STORE_ENDPOINT = "direct:storeMessage";
    public static final String DEFAULT_MESSAGE_INSERT_ENDPOINT = "direct:insertMessage";
    public static final String DEFAULT_MESSAGE_UPDATE_ENDPOINT = "direct:updateMessage";

    public static final String MESSAGE_STORE_REF = "JentrataMessageStoreRef";
    public static final String JENTRATA_MESSAGE_ID = "JentrataMessageId";

    void store(@Body InputStream message, Exchange exchange);

    void storeMessage(Exchange exchange);

    void updateMessage(@Header(EbmsConstants.MESSAGE_ID)String messageId,
                       @Header(EbmsConstants.MESSAGE_STATUS)MessageStatusType status,
                       @Header(EbmsConstants.MESSAGE_STATUS_DESCRIPTION)String statusDescription);

    InputStream findByMessageRefId(Object messageRefId);
}
