package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.messaging.Message;
import org.jentrata.ebms.messaging.MessageStore;
import org.jentrata.ebms.messaging.internal.sql.RepositoryManagerFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

/**
 * unit test for org.jentrata.ebms.messaging.internal.JDBCMessageStore
 *
 * @author aaronwalker
 */
public class JDBCMessageStoreTest extends CamelTestSupport {

    private JdbcConnectionPool dataSource;
    private JDBCMessageStore messageStore;

    @Test
    public void testShouldCreateMessageStoreTablesByDefault() throws Exception {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement st = conn.createStatement()) {
                assertTableGotCreated(st,"repository");
                assertTableGotCreated(st,"message");
            }
        }
    }

    @Test
    public void testShouldStoreMimeMessage() throws Exception {
        File body = fileFromClasspath("simple-as4-receipt.xml");
        String contentType = "Multipart/Related; boundary=\"----=_Part_7_10584188.1123489648993\"; type=\"application/soap+xml\"; start=\"<soapPart@jentrata.org>\"";
        String messageId = "testMimeMessage1";
        assertStoredMessage(messageId, contentType, body, MessageType.USER_MESSAGE);
    }

    @Test
    public void testShouldStoreSoapMessage() throws Exception {
        String contentType = "application/soap+xml";
        File body = fileFromClasspath("simple-as4-receipt.xml");
        String messageId = "testSoapMessage1";
        assertStoredMessage(messageId, contentType, body, MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE);
    }

    @Test
    public void testUpdateStoreMimeMessage() throws Exception {
        File body = fileFromClasspath("simple-as4-receipt.xml");
        String contentType = "Multipart/Related; boundary=\"----=_Part_7_10584188.1123489648993\"; type=\"application/soap+xml\"; start=\"<soapPart@jentrata.org>\"";
        String messageId = "testMimeMessage1";
        assertStoredMessage(messageId, contentType, body, MessageType.USER_MESSAGE);
        messageStore.updateMessage(messageId, EbmsConstants.MESSAGE_DIRECTION_INBOUND, MessageStatusType.RECEIVED,"Message Received");
        try(Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("select * from message where message_id = ?")) {
                st.setString(1,messageId);
                ResultSet resultSet = st.executeQuery();
                assertThat(resultSet.next(),is(true));
                assertThat(resultSet.getString("status"),equalTo("RECEIVED"));
                assertThat(resultSet.getString("status_description"),equalTo("Message Received"));
            }
        }
    }

    @Test
    public void testFindByMessageId() throws Exception {
        String contentType = "application/soap+xml";
        File body = fileFromClasspath("simple-as4-receipt.xml");
        String messageId = "testSoapMessage1";
        assertStoredMessage(messageId, contentType, body, MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE);
        messageStore.updateMessage(messageId, EbmsConstants.MESSAGE_DIRECTION_INBOUND, MessageStatusType.RECEIVED,"Message Received");
        Message message = messageStore.findByMessageId("testSoapMessage1",EbmsConstants.MESSAGE_DIRECTION_INBOUND);
        assertThat(message,notNullValue());
        assertThat(message.getMessageId(),equalTo("testSoapMessage1"));
        assertThat(message.getStatus(),equalTo(MessageStatusType.RECEIVED));
        assertThat(message.getStatusDescription(),equalTo("Message Received"));
    }

    @Test
    public void testFindPayloadById() throws Exception {
        testFindByMessageId();
        InputStream stream = messageStore.findPayloadById("testSoapMessage1");
        assertThat(stream,notNullValue());
        assertThat(IOUtils.toString(stream),equalTo(IOUtils.toString(new FileInputStream(fileFromClasspath("simple-as4-receipt.xml")))));
    }

    private void assertStoredMessage(String messageId, String contentType, File body, MessageType messageType) throws SQLException, IOException {
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.EBMS_VERSION,EbmsConstants.EBMS_V3);
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,messageId);
        request.getIn().setHeader(EbmsConstants.MESSAGE_DIRECTION,EbmsConstants.MESSAGE_DIRECTION_INBOUND);
        request.getIn().setHeader(EbmsConstants.MESSAGE_TYPE,messageType);
        request.getIn().setHeader(EbmsConstants.CONTENT_TYPE,contentType);
        request.getIn().setHeader(EbmsConstants.CPA_ID,"testCPAId");

        request.getIn().setBody(body);
        context().createProducerTemplate().send(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT,request);
        try(Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("select * from repository where message_id = ?")) {
                st.setString(1,messageId);
                ResultSet resultSet = st.executeQuery();
                assertThat(resultSet.next(),is(true));
                assertThat(resultSet.getString("message_box"),equalTo(EbmsConstants.MESSAGE_DIRECTION_INBOUND));
                assertThat(resultSet.getString("content_type"),equalTo(contentType));
                assertThat(resultSet.getDate("time_stamp"),notNullValue());
                assertThat(IOUtils.toString(resultSet.getBinaryStream("content")),equalTo(IOUtils.toString(new FileInputStream(body))));
            }
            try (PreparedStatement st = conn.prepareStatement("select * from message where message_id = ?")){
                st.setString(1,messageId);
                ResultSet resultSet = st.executeQuery();
                assertThat(resultSet.next(),is(true));
                assertThat(resultSet.getString("message_box"),equalTo(EbmsConstants.MESSAGE_DIRECTION_INBOUND));
                assertThat(resultSet.getString("message_type"),equalTo(messageType.name()));
                assertThat(resultSet.getString("cpa_id"),equalTo("testCPAId"));
                assertThat(resultSet.getDate("time_stamp"),notNullValue());
            }
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + getTestMethodName(), "sa", "sa");
        assertThatTableDoesNotExist("repository");
        RepositoryManagerFactory repositoryManagerFactory = new RepositoryManagerFactory();
        repositoryManagerFactory.setDataSource(dataSource);
        messageStore = new JDBCMessageStore();
        messageStore.setRepositoryManager(repositoryManagerFactory.createRepositoryManager());
        messageStore.init();
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT)
                    .bean(messageStore,"store")
                    .bean(messageStore,"storeMessage")
                .routeId("testPostsgresMessageStore");
            }
        };
    }

    private void assertThatTableDoesNotExist(String tableName) throws SQLException {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement st = conn.createStatement()) {
                ResultSet resultSet = st.executeQuery("select count(*) from " + tableName);
                fail("DB tables shouldn't exists");
            } catch (SQLException ex) {

            }
        }
    }

    private static void assertTableGotCreated(Statement st, String tableName) throws SQLException {
        ResultSet resultSet = st.executeQuery("select count(*) from " + tableName);
        assertThat(resultSet.next(),is(true));
        assertThat(resultSet.getInt(1), equalTo(0));
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
