package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.AttachmentRequestCallback;
import org.apache.wss4j.common.ext.AttachmentResultCallback;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSEncryptionPart;
import org.apache.wss4j.dom.WSSConfig;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.wss4j.dom.message.WSSecUsernameToken;
import org.apache.xml.security.utils.Base64;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.cpa.pmode.Signature;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.jentrata.ebms.internal.messaging.AttachmentDataSource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for org.jentrata.ebms.as4.internal.routes.WSSERouteBuilder
 *
 * @author aaronwalker
 */
public class WSSERouteBuilderTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(WSSERouteBuilderTest.class);

    private Crypto crypto = null;

    @Test
    public void testInvalidUsernameToken() throws Exception {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(generateSoapMessage("unknown"));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement());
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(request.getIn().getHeader(EbmsConstants.SECURITY_RESULTS,Exception.class),instanceOf(WSSecurityException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.SECURITY_CHECK,Boolean.class),is(false));
    }

    @Test
    public void testEmptyUsernameToken() throws Exception {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(loadSoapMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement());
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(request.isFailed(),is(false));
        assertThat(response.getIn().getHeader(EbmsConstants.SECURITY_CHECK,Boolean.class),is(false));

    }

    @Test
    public void testAddSecurityToHeader() throws Exception {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(loadSoapMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement());
        Exchange response = context().createProducerTemplate().send("direct:wsseAddSecurityToHeader",request);

        Document body = response.getIn().getBody(Document.class);
        System.out.println(XMLUtils.PrettyDocumentToString(body));

        assertThat(body, hasXPath("//*[local-name()='UsernameToken']"));
        assertThat(body, hasXPath("//*[local-name()='Username' and text()='jentrata']"));
        assertThat(body,hasXPath("//*[local-name()='Password']"));
        assertThat(body,hasXPath("//*[local-name()='Created']"));

    }

    @Test
    public void testSignedMessageWithAttachments() throws Exception {

        byte [] data = IOUtils.toByteArray(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Attachment attachment = new Attachment();
        attachment.setId("attachment1234@jentrata.org");
        attachment.setMimeType("application/xml");
        attachment.setSourceStream(new ByteArrayInputStream(data));
        AttachmentDataSource dataSource = new AttachmentDataSource(attachment);

        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(generateSoapMessage("jentrata", attachment));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement("jentrata"));
        request.getIn().addAttachment(attachment.getId(),new DataHandler(dataSource));

        //perform the signing verification and security check
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(request.isFailed(),is(false));
        assertThat(response.getIn().getHeader(EbmsConstants.SECURITY_CHECK,Boolean.class),is(true));
        assertThat(response.getIn().hasAttachments(),is(true));
        assertThat(response.getIn().getAttachment("attachment1234@jentrata.org"),notNullValue());
        assertThat(IOUtils.toByteArray(request.getIn().getAttachment("attachment1234@jentrata.org").getInputStream()),equalTo(data));
    }

    @Test
    public void testAttachmentWithInvalidSignature() throws Exception {
        byte [] data = IOUtils.toByteArray(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        Attachment attachment = new Attachment();
        attachment.setId("attachment1234@jentrata.org");
        attachment.setMimeType("application/xml");
        attachment.setSourceStream(new ByteArrayInputStream(data));

        Attachment badAttachment = new Attachment();
        attachment.setId("attachment1234@jentrata.org");
        attachment.setMimeType("application/xml");
        attachment.setSourceStream(new ByteArrayInputStream("<bad>Im a bad guy who has modified your attachment</bad>".getBytes()));
        AttachmentDataSource badDataSource = new AttachmentDataSource(badAttachment);

        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(generateSoapMessage("jentrata", attachment));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement("jentrata"));
        request.getIn().addAttachment(attachment.getId(),new DataHandler(badDataSource));

        //perform the signing verification and security check
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(request.getIn().getHeader(EbmsConstants.SECURITY_RESULTS,Exception.class),instanceOf(WSSecurityException.class));
        assertThat(response.getIn().getHeader(EbmsConstants.SECURITY_CHECK,Boolean.class),is(false));
    }

    @Test
    public void testAddSignatureSecurityToHeader() throws Exception {

        byte [] data = IOUtils.toByteArray(new FileInputStream(fileFromClasspath("sample-payload.xml")));
        List<Map<String,Object>> payloads = new ArrayList<>();
        Map<String,Object> payload = new HashMap<>();
        payload.put("payloadId", "attachment1234@jentrata.org");
        payload.put("contentType", "application/xml");
        payload.put("charset", "utf-8");
        payload.put("partProperties", "sourceABN=123456789;targetABN=987654321");
        payload.put("schema", "test");
        payload.put("content", data);

        payloads.add(payload);

        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(loadSoapMessage());
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement("jentrata",true));
        request.getIn().setHeader(EbmsConstants.MESSAGE_PAYLOADS,payloads);
        Exchange response = context().createProducerTemplate().send("direct:wsseAddSecurityToHeader",request);

        Document body = response.getIn().getBody(Document.class);
        System.out.println(XMLUtils.PrettyDocumentToString(body));

        assertThat(body, hasXPath("//*[local-name()='UsernameToken']"));
        assertThat(body, hasXPath("//*[local-name()='Username' and text()='jentrata']"));
        assertThat(body,hasXPath("//*[local-name()='Password']"));
        assertThat(body,hasXPath("//*[local-name()='Created']"));


        assertThat(body, hasXPath("//*[local-name()='Signature']"));
        assertThat(body, hasXPath("//@*[name()='URI' and .='cid:attachment1234@jentrata.org']"));
        assertThat(body, hasXPath("//@*[name()='href' and .='cid:attachment1234@jentrata.org']"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        WSSConfig.init();
        crypto = CryptoFactory.getInstance();
        WSSERouteBuilder routeBuilder = new WSSERouteBuilder();
        routeBuilder.setUserTokenCallbackHandler(new EncodedPasswordCallbackHandler());
        routeBuilder.setCrypto(crypto);
        return routeBuilder;
    }

    private String generateSoapMessage(String username, final Attachment...attachments) throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordsAreEncoded(true);
        builder.setUserInfo(username, getEncodedPassword());
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = loadSoapMessage();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        if(attachments != null && attachments.length > 0) {
            List<WSEncryptionPart> parts = new ArrayList<>();
            parts.add(new WSEncryptionPart("Messaging","http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",""));
            parts.add(new WSEncryptionPart("Body", "http://www.w3.org/2003/05/soap-envelope", "Content"));
            parts.add(new WSEncryptionPart("cid:Attachments", "Content"));

            WSSecSignature signature = new WSSecSignature();
            signature.setUserInfo("jentrata", "security");
            signature.setSignatureAlgorithm(WSConstants.RSA);
            signature.setDigestAlgo(WSConstants.SHA256);
            signature.setParts(parts);
            signature.setAttachmentCallbackHandler(new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for(int i=0;i<callbacks.length;i++) {
                        if (callbacks[i] instanceof AttachmentRequestCallback) {
                            AttachmentRequestCallback attachmentRequestCallback = (AttachmentRequestCallback) callbacks[i];
                            List<Attachment> attachmentList = new ArrayList<>();
                            attachmentList.add(attachments[i]);
                            attachmentRequestCallback.setAttachments(attachmentList);
                        } else {
                            AttachmentResultCallback attachmentResultCallback = (AttachmentResultCallback) callbacks[i];
                            attachments[i] = attachmentResultCallback.getAttachment();
                        }
                    }
                }
            });
            signedDoc = signature.build(signedDoc, crypto, secHeader);
        }
        String outputString = XMLUtils.PrettyDocumentToString(signedDoc);
        LOG.info(outputString);
        return outputString;
    }


    private Document loadSoapMessage() throws Exception {
        return context().getTypeConverter().convertTo(Document.class, new FileInputStream(fileFromClasspath("sample-wsse-soap.xml")));
    }

    private PartnerAgreement generateAgreement() {
        return generateAgreement("jentrata");
    }

    private PartnerAgreement generateAgreement(String username) {
        return generateAgreement("jentrata",false);
    }

    private PartnerAgreement generateAgreement(String username, boolean signatureEnabled) {
        PartnerAgreement agreement = new PartnerAgreement();
        agreement.setCpaId("JentrataTestCPA");
        Security security = new Security();
        UsernameToken token = new UsernameToken();
        token.setUsername(username);
        token.setPassword(getEncodedPassword());
        security.setSecurityToken(token);
        agreement.setSecurity(security);
        if(signatureEnabled) {
            Signature signature = new Signature();
            signature.setKeyStoreAlias(username);
            signature.setKeyStorePass("security");
            security.setSignature(signature);
        }
        return agreement;
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }

    private String getEncodedPassword() {
        try {
            return Base64.encode(MessageDigest.getInstance("SHA-1").digest("verySecret".getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private class EncodedPasswordCallbackHandler implements CallbackHandler {
        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callback;
                    if (pc.getUsage() == WSPasswordCallback.Usage.USERNAME_TOKEN) {
                        if ("jentrata".equals(pc.getIdentifier())) {
                            pc.setPassword(getEncodedPassword());
                        } else if("16c73ab6-b892-458f-abf5-2f875f74882e".equals(pc.getIdentifier())) {
                            pc.setPassword(getEncodedPassword());
                        }
                    }
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        }
    }

}
