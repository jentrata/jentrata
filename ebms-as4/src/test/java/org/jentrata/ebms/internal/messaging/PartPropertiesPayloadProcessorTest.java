package org.jentrata.ebms.internal.messaging;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.messaging.SplitAttachmentsToBody;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * Unit test for org.jentrata.ebms.internal.messaging.PartPropertiesPayloadProcessor
 *
 * @author aaronwalker
 */
public class PartPropertiesPayloadProcessorTest extends CamelTestSupport {

    @Test
    public void testPartPropertiesPayloadProcessor() throws Exception{
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.CONTENT_ID,"payload-id@jentrata.org");
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/xml");
        request.getIn().setHeader(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, IOUtils.toString(new FileInputStream(fileFromClasspath("soapenv-user-message.xml"))));
        request.getIn().setBody("<invoice>123456789</invoice>");

        Exchange response = context().createProducerTemplate().send("direct:testPartPropertiesPayloadProcessor",request);

        assertThat(response.getIn().getHeader("PartID",String.class),equalTo("payload-id@jentrata.org"));
        assertThat(response.getIn().getHeader("MimeType",String.class),equalTo("text/xml"));
        assertThat(response.getIn().getHeader("CharacterSet",String.class),equalTo("UTF-8"));
    }

    @Test
    public void testSoapBodyPartPropertiesPayloadProcessor() throws Exception{
        Exchange request = new DefaultExchange(context());
        request.getIn().setHeader(EbmsConstants.CONTENT_ID,EbmsConstants.SOAP_BODY_PAYLOAD_ID);
        request.getIn().setHeader(Exchange.CONTENT_TYPE,"application/xml");
        request.getIn().setHeader(SplitAttachmentsToBody.ORIGINAL_MESSAGE_BODY, IOUtils.toString(new FileInputStream(fileFromClasspath("simple-as4-with-soap-body.xml"))));
        request.getIn().setBody(" <invoice id=\"123\"/>");

        Exchange response = context().createProducerTemplate().send("direct:testPartPropertiesPayloadProcessor",request);

        assertThat(response.getIn().getHeader("PartID",String.class),equalTo("soapbody"));
        assertThat(response.getIn().getHeader("MimeType",String.class),equalTo("application/xml"));
        assertThat(response.getIn().getHeader("CharacterSet",String.class),equalToIgnoringCase("UTF-8"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:testPartPropertiesPayloadProcessor")
                    .bean(new PartPropertiesPayloadProcessor())
                .routeId("testPartPropertiesPayloadProcessor");
            }
        };
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
