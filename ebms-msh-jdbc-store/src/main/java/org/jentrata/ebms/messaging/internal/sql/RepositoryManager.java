package org.jentrata.ebms.messaging.internal.sql;

import java.io.InputStream;

/**
 * RepositoryManager interface
 *
 * @author aaronwalker
 */
public interface RepositoryManager {
    void createTablesIfNotExists();
    void insertIntoRepository(String messageId, String contentType, String messageDirection, long contentLength, InputStream content);
}
