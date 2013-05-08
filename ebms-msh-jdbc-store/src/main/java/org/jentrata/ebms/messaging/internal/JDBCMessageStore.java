package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Exchange;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ResourceHelper;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.messaging.MessageStoreException;
import org.jentrata.ebms.messaging.internal.sql.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Store the raw message in Postgres
 *
 * @author aaronwalker
 */
public class JDBCMessageStore implements MessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCMessageStore.class);

    private RepositoryManager repositoryManager;
    private boolean createTables = true;

    public void init() throws IOException {
        if(createTables) {
            LOG.info("Checking if Database tables exist");
            repositoryManager.createTablesIfNotExists();
        } else {
            LOG.info("Database should have already been created");
        }
    }

    @Override
    public void store(InputStream message, Exchange exchange) {
        String messageId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class);
        String messageDirection = exchange.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION,String.class);
        String contentType = exchange.getIn().getHeader(EbmsConstants.CONTENT_TYPE,String.class);
        long contentLength = exchange.getIn().getHeader(EbmsConstants.CONTENT_LENGTH,0L,Long.class);
        String cpaId = exchange.getIn().getHeader(EbmsConstants.CPA_ID,String.class);
        repositoryManager.insertIntoRepository(messageId, contentType, messageDirection, contentLength, message);

    }

    public void storeMessage(Exchange exchange) {
        String messageId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class);
        String messageDirection = exchange.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION,String.class);
        String cpaId = exchange.getIn().getHeader(EbmsConstants.CPA_ID,String.class);
        String conversationId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,String.class);
        repositoryManager.insertMessage(messageId,messageDirection,cpaId,conversationId);
    }

    @Override
    public void updateMessage(String messageId, MessageStatusType status, String statusDescription) {
        repositoryManager.updateMessage(messageId,status,statusDescription);
    }

    @Override
    public InputStream findByMessageRefId(Object messageRefId) {
        return new ByteArrayInputStream("".getBytes());
    }

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public boolean isCreateTables() {
        return createTables;
    }

    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }
}
