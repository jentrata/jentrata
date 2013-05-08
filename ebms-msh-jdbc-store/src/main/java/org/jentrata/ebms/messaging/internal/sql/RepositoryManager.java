package org.jentrata.ebms.messaging.internal.sql;

import org.jentrata.ebms.MessageStatusType;

import java.io.InputStream;

/**
 * RepositoryManager interface
 *
 * @author aaronwalker
 */
public interface RepositoryManager {
    void createTablesIfNotExists();
    void insertIntoRepository(String messageId, String contentType, String messageDirection, long contentLength, InputStream content);
    void updateMessage(String messageId, MessageStatusType status, String statusDescription);
    void insertMessage(String messageId, String messageDirection, String cpaId, String conversationId);
}
