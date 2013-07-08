package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ResourceHelper;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.messaging.MessageStoreException;
import org.jentrata.ebms.messaging.UUIDGenerator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store the raw message in Postgres
 *
 * @author aaronwalker
 */
public class JDBCMessageStore implements MessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(JDBCMessageStore.class);

    private RepositoryManager repositoryManager;
    private UUIDGenerator uuidGenerator;
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
        boolean duplicate = repositoryManager.isDuplicate(messageId,messageDirection);
        String duplicateMessageId = null;
        if(duplicate) {
            duplicateMessageId = uuidGenerator.generateId();
            LOG.info("Message " + messageId + " is a duplicate new message Id " + duplicateMessageId);
            exchange.getIn().setHeader(EbmsConstants.DUPLICATE_MESSAGE,true);
            exchange.getIn().setHeader(EbmsConstants.DUPLICATE_MESSAGE_ID,duplicateMessageId);
            repositoryManager.insertIntoRepository(duplicateMessageId, contentType, messageDirection, contentLength, message, messageId);
        }  else {
            repositoryManager.insertIntoRepository(messageId, contentType, messageDirection, contentLength, message, messageId);
        }
    }

    public void storeMessage(Exchange exchange) {
        String messageId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ID,String.class);
        String messageDirection = exchange.getIn().getHeader(EbmsConstants.MESSAGE_DIRECTION,String.class);
        MessageType messageType = exchange.getIn().getHeader(EbmsConstants.MESSAGE_TYPE,MessageType.class);
        String cpaId = exchange.getIn().getHeader(EbmsConstants.CPA_ID,String.class);
        String conversationId = exchange.getIn().getHeader(EbmsConstants.MESSAGE_CONVERSATION_ID,String.class);
        String refMessageID = exchange.getIn().getHeader(EbmsConstants.REF_TO_MESSAGE_ID, String.class);
        String duplicateMessageId = exchange.getIn().getHeader(EbmsConstants.DUPLICATE_MESSAGE_ID, String.class);
       if(duplicateMessageId != null) {
           repositoryManager.insertMessage(duplicateMessageId,messageDirection,messageType,cpaId,conversationId,refMessageID);
       } else {
            repositoryManager.insertMessage(messageId,messageDirection,messageType,cpaId,conversationId,refMessageID);
       }
    }

    @Override
    public void updateMessage(String messageId, String messageDirection, MessageStatusType status, String statusDescription) {
        repositoryManager.updateMessage(messageId,messageDirection,status,statusDescription);
    }

    @Override
    public Message findByMessageId(String messageId, String messageDirection) {
        Map<String,Object> fields = new HashMap<>();
        fields.put("message_id",messageId);
        fields.put("message_box",messageDirection);
        List<Message> messages = repositoryManager.selectMessageBy(fields);
        if(messages.size() > 0) {
            return messages.get(0);
        }
        return null;
    }

    @Override
    public InputStream findPayloadById(String messageId) {
        InputStream payload = repositoryManager.selectRepositoryBy("message_id",messageId);
        return payload;
    }

    @Override
    public List<Message> findByMessageStatus(String messageDirection, String status) {
        Map<String,Object> fields = new HashMap<>();
        fields.put("status",status);
        fields.put("message_box",messageDirection);
        fields.put("orderByDesc","time_stamp");
        fields.put("maxResults",100);
        List<Message> messages = repositoryManager.selectMessageBy(fields);
        return messages;
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

    public UUIDGenerator getUuidGenerator() {
        return uuidGenerator;
    }

    public void setUuidGenerator(UUIDGenerator uuidGenerator) {
        this.uuidGenerator = uuidGenerator;
    }
}
