package org.jentrata.ebms.messaging;

/**
 * Top level exception when interacting with the MessageStore
 *
 * @author aaronwalker
 */
public class MessageStoreException extends RuntimeException {

    public MessageStoreException() {
    }

    public MessageStoreException(String message) {
        super(message);
    }

    public MessageStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageStoreException(Throwable cause) {
        super(cause);
    }

    public MessageStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
