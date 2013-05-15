package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.jentrata.ebms.EbmsConstants;
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
    private CallbackHandler callbackHandler;

    @Override
    public void configure() throws Exception {
        from(wsseSecurityCheck)
            .log(LoggingLevel.INFO,"Performing WSSE check for ${headers.JentrataCPAId} - on message:${headers.JentrataMessageID}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    Document signedDoc = exchange.getIn().getBody(Document.class);
                    WSSecurityEngine newEngine = new WSSecurityEngine();
                    newEngine.getWssConfig().setPasswordsAreEncoded(true);
                    List<WSSecurityEngineResult> results = newEngine.processSecurityHeader(signedDoc, null, callbackHandler, null);
                    if(results != null && results.size() > 0) {
                        exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,results.get(0).get(WSSecurityEngineResult.TAG_VALIDATED_TOKEN));
                    } else {
                        exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,Boolean.FALSE);
                    }
                }
            })
        .routeId("_jentrataWSSESecurityCheck");
    }

    public String getWsseSecurityCheck() {
        return wsseSecurityCheck;
    }

    public void setWsseSecurityCheck(String wsseSecurityCheck) {
        this.wsseSecurityCheck = wsseSecurityCheck;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }
}
