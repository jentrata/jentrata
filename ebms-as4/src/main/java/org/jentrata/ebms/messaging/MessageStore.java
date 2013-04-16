package org.jentrata.ebms.messaging;

import org.apache.camel.Body;
import org.apache.camel.Exchange;

import java.io.InputStream;

/**
 *  A ebMS MessageStore interface
 *
 * @author aaronwalker
 */
public interface MessageStore {

    public static final String DEFAULT_MESSAGE_STORE_ENDPOINT = "direct:storeMessage";

    public static final String MESSAGE_STORE_REF = "JentrataMessageStoreRef";
    public static final String JENTRATA_MESSAGE_ID = "JentrataMessageId";

    public void store(@Body InputStream input, Exchange exchange);

    public InputStream findByMessageRef(Object messageRef);
}
