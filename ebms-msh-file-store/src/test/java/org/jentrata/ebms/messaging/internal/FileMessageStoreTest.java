package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageStatusType;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit Test for org.jentrata.ebms.messaging.internal.FileMessageStore
 *
 * @author aaronwalker
 */
public class FileMessageStoreTest extends CamelTestSupport {

    private FileMessageStore messageStore;
    private String baseDir;

    @Test
    public void testFileMessageStore() throws IOException {
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.EBMS_VERSION,EbmsConstants.EBMS_V3);
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID,"testMessageID");
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        Exchange response = context().createProducerTemplate().send(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT,request);

        String msgId = response.getIn().getHeader(MessageStore.JENTRATA_MESSAGE_ID, String.class);
        Object msgStoreRef = response.getIn().getHeader(MessageStore.MESSAGE_STORE_REF);
        assertThat(msgId,equalTo("testMessageID"));
        assertThat(IOUtils.toString(messageStore.findByMessageRefId(msgStoreRef)),equalTo("test"));
    }

    @Test
    public void testUpdateMessageInMessageStore() throws Exception {
        messageStore.updateMessage("testMessageID", MessageStatusType.RECEIVED,"Received");
        File expectedFile = new File(baseDir,"testMessageID.RECEIVED");
        assertThat(expectedFile.exists(),is(true));
        assertThat("Received", Matchers.equalTo(IOUtils.toString(new FileInputStream(expectedFile))));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        messageStore = new FileMessageStore();
        baseDir = System.getProperty("java.io.tmpdir");
        messageStore.setBaseDir(baseDir);
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
            from(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT)
                    .bean(messageStore,"store")
                    .routeId("_jentrataMessageStoreTest");
            }
        };
    }
}
