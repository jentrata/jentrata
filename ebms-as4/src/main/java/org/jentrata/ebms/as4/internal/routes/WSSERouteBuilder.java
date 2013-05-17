package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;
import java.util.List;


/**
 * Provides WSSE integration for the incoming AS4 Message
 *
 * @author aaronwalker
 */
public class WSSERouteBuilder extends RouteBuilder {

    private String wsseSecurityCheck = "direct:wsseSecurityCheck";
    private String wsseAddSecurityToHeader = "direct:wsseAddSecurityToHeader";
    private CallbackHandler callbackHandler;

    @Override
    public void configure() throws Exception {
        from(wsseSecurityCheck)
            .log(LoggingLevel.INFO,"Performing WSSE check for ${headers.JentrataCPAId} - on message:${headers.JentrataMessageID}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                    if (agreement.hasSecurityToken() && agreement.getSecurity().getSecurityToken() instanceof UsernameToken) {
                        Document signedDoc = exchange.getIn().getBody(Document.class);
                        UsernameToken token = (UsernameToken) agreement.getSecurity().getSecurityToken();
                        WSSecurityEngine newEngine = new WSSecurityEngine();
                        newEngine.getWssConfig().setPasswordsAreEncoded(token.isDigest());
                        List<WSSecurityEngineResult> results = newEngine.processSecurityHeader(signedDoc, null, callbackHandler, null);
                        if(results != null && results.size() > 0) {
                            exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,results.get(0).get(WSSecurityEngineResult.TAG_VALIDATED_TOKEN));
                        } else {
                            exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,Boolean.FALSE);
                        }
                    } else {
                        exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,Boolean.TRUE);
                    }
                }
            })
        .routeId("_jentrataWSSESecurityCheck");

        from(wsseAddSecurityToHeader)
            .log(LoggingLevel.INFO, "Adding UsernameToken for ${headers.JentrataCPAId} - on message:${headers.JentrataMessageID}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    Document message = exchange.getIn().getBody(Document.class);
                    PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                    if (agreement.hasSecurityToken() && agreement.getSecurity().getSecurityToken() instanceof UsernameToken) {
                        UsernameToken token = (UsernameToken) agreement.getSecurity().getSecurityToken();
                        WSSecUsernameToken builder = new WSSecUsernameToken();
                        builder.setPasswordsAreEncoded(token.isDigest());
                        builder.setUserInfo(token.getUsername(),token.getPassword());

                        WSSecHeader secHeader = new WSSecHeader();
                        secHeader.insertSecurityHeader(message);
                        Document signedDoc = builder.build(message, secHeader);

                        exchange.getIn().setBody(signedDoc);
                    }
                }
            })
        .routeId("_jentrataWSSEInsertUsernameToken");
    }

    public String getWsseSecurityCheck() {
        return wsseSecurityCheck;
    }

    public void setWsseSecurityCheck(String wsseSecurityCheck) {
        this.wsseSecurityCheck = wsseSecurityCheck;
    }

    public String getWsseAddSecurityToHeader() {
        return wsseAddSecurityToHeader;
    }

    public void setWsseAddSecurityToHeader(String wsseAddSecurityToHeader) {
        this.wsseAddSecurityToHeader = wsseAddSecurityToHeader;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }
}
