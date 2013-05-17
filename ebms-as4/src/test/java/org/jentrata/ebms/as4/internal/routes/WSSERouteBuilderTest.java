package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.XMLUtils;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for org.jentrata.ebms.as4.internal.routes.WSSERouteBuilder
 *
 * @author aaronwalker
 */
public class WSSERouteBuilderTest extends CamelTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(WSSERouteBuilderTest.class);

    @Test
    public void testValidUsernameToken() throws Exception {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(generateSoapMessage("jentrata"));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement());
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(response.getIn().getHeader(EbmsConstants.SECURITY_CHECK,Boolean.class),is(true));
    }

    @Test
    public void testInvalidUsernameToken() throws Exception {
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody(generateSoapMessage("unknown"));
        request.getIn().setHeader(EbmsConstants.MESSAGE_ID, "testMSG-0001");
        request.getIn().setHeader(EbmsConstants.CPA_ID,"JentrataTestCPA");
        request.getIn().setHeader(EbmsConstants.CPA,generateAgreement());
        Exchange response = context().createProducerTemplate().send("direct:wsseSecurityCheck",request);
        assertThat(request.isFailed(),is(true));
        assertThat(request.getException(),instanceOf(WSSecurityException.class));
        assertThat(request.getException().getMessage(),containsString("The security token could not be authenticated or authorized"));
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

    private PartnerAgreement generateAgreement() {
        PartnerAgreement agreement = new PartnerAgreement();
        agreement.setCpaId("JentrataTestCPA");
        Security security = new Security();
        UsernameToken token = new UsernameToken();
        token.setUsername("jentrata");
        token.setPassword(getEncodedPassword());
        security.setSecurityToken(token);
        agreement.setSecurity(security);
        return agreement;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        WSSERouteBuilder routeBuilder = new WSSERouteBuilder();
        routeBuilder.setCallbackHandler(new EncodedPasswordCallbackHandler());
        return routeBuilder;
    }

    private String generateSoapMessage(String username) throws Exception {
        WSSecUsernameToken builder = new WSSecUsernameToken();
        builder.setPasswordsAreEncoded(true);
        builder.setUserInfo(username, getEncodedPassword());
        LOG.info("Before adding UsernameToken PW Digest....");
        Document doc = loadSoapMessage();
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        Document signedDoc = builder.build(doc, secHeader);

        LOG.info("Message with UserNameToken PW Digest:");
        String outputString = org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        LOG.info(outputString);
        LOG.info("After adding UsernameToken PW Digest....");

        return outputString;
    }

    private Document loadSoapMessage() throws Exception {
        return context().getTypeConverter().convertTo(Document.class, new FileInputStream(fileFromClasspath("sample-wsse-soap.xml")));
    }

    private String getEncodedPassword() {
        try {
            return Base64.encode(MessageDigest.getInstance("SHA-1").digest("verySecret".getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }

    private class EncodedPasswordCallbackHandler implements CallbackHandler {
        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callback;
                    if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
                        if ("jentrata".equals(pc.getIdentifier())) {
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
