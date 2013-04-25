package org.jentrata.ebms.messaging.internal.sql;

import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.messaging.MessageStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

/**
 * TODO: add description
 *
 * @author aaronwalker
 */
public abstract class AbstractRepositoryManager implements RepositoryManager {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected DataSource dataSource;

    public AbstractRepositoryManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createTablesIfNotExists() {
        try(Connection connection = dataSource.getConnection()) {
            try(Statement stmt = connection.createStatement()) {
                int result = stmt.executeUpdate(getCreateSQL());
                if(result > 0) {
                    LOG.info("Message Store tables successfully created");
                }
            }
        } catch (Exception e) {
            throw new MessageStoreException("unable to create/check database tables:" + e,e);
        }
    }

    @Override
    public void insertIntoRepository(String messageId, String contentType, String messageDirection, long contentLength, InputStream content) {
        try(Connection connection = dataSource.getConnection()) {
            String sql = getInsertSQL();
            try(PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1,messageId);
                stmt.setString(2,contentType);
                stmt.setTimestamp(3, new Timestamp(new Date().getTime()));
                stmt.setString(4,messageDirection);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(content,bos);
                stmt.setBytes(5, bos.toByteArray());
                int result = stmt.executeUpdate();
                if(result != 1) {
                    throw new MessageStoreException("failed to write message to store");
                }
            }
        } catch (SQLException|IOException e) {
            throw new MessageStoreException("failed to write message to store:" + e,e);
        }    }

    protected String getInsertSQL() {
        return "INSERT INTO repository (message_id,content_type,time_stamp,message_box,content) VALUES(?,?,?,?,?)";
    }

    protected abstract String getCreateSQL();
}
