package org.jentrata.ebms.messaging;

import java.io.InputStream;

/**
 * A reference to a message payload
 *
 * @author aaronwalker
 */
public interface MessagePayload {

    /**
     * The Content-ID of the payload as it's represented in the original message
     *
     * @return
     */
    public String getContentId();

    /**
     * The Mime Content-Type of  the payload
     * @return
     */
    public String getContentType();

    /**
     * The payload Contents
     * @return
     */
    public InputStream getContents();
}
