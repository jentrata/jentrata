package org.jentrata.ebms.messaging.internal;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.MessageStore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

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
        request.getIn().setBody(new ByteArrayInputStream("test".getBytes()));
        Exchange response = context().createProducerTemplate().send(MessageStore.DEFAULT_MESSAGE_STORE_ENDPOINT,request);

        String msgId = response.getIn().getHeader(MessageStore.JENTRATA_MESSAGE_ID, String.class);
        Object msgStoreRef = response.getIn().getHeader(MessageStore.MESSAGE_STORE_REF);
        assertThat(msgId,equalTo(request.getIn().getMessageId()));
        assertThat(IOUtils.toString(messageStore.findByMessageRefId(msgStoreRef)),equalTo("test"));
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
