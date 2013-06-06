package org.jentrata.ebms.messaging.internal.sql;

import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.Message;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * RepositoryManager interface
 *
 * @author aaronwalker
 */
public interface RepositoryManager {
    void createTablesIfNotExists();
    void insertIntoRepository(String messageId, String contentType, String messageDirection, long contentLength, InputStream content);
    void updateMessage(String messageId, String messageDirection, MessageStatusType status, String statusDescription);
    void insertMessage(String messageId, String messageDirection, MessageType messageType, String cpaId, String conversationId, String refMessageID);
    List<Message> selectMessageBy(String columnName, String value);
    List<Message> selectMessageBy(Map<String,Object> fields);
    InputStream selectRepositoryBy(String columnName, String value);
}
