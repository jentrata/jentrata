package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.ext.Attachment;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSEncryptionPart;
import org.apache.wss4j.dom.WSSecurityEngine;
import org.apache.wss4j.dom.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.wss4j.dom.message.WSSecUsernameToken;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.Signature;
import org.jentrata.ebms.cpa.pmode.SignaturePart;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.jentrata.ebms.internal.messaging.AttachmentCallbackHandler;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.security.auth.callback.CallbackHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Provides WSSE integration for the incoming AS4 Message
 *
 * @author aaronwalker
 */
public class WSSERouteBuilder extends RouteBuilder {

    private String wsseSecurityCheck = "direct:wsseSecurityCheck";
    private String wsseAddSecurityToHeader = "direct:wsseAddSecurityToHeader";
    private CallbackHandler userTokenCallbackHandler;
    private Crypto crypto;

    @Override
    public void configure() throws Exception {
        final WSSecurityEngine securityEngine = new WSSecurityEngine();
        from(wsseSecurityCheck)
            .onException(WSSecurityException.class)
                .setHeader(EbmsConstants.SECURITY_CHECK,constant(Boolean.FALSE))
            .end()
            .log(LoggingLevel.INFO, "Performing WSSE check for ${headers.JentrataCPAId} - on message:${headers.JentrataMessageID}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                    if (agreement.hasSecurityToken() && agreement.getSecurity().getSecurityToken() instanceof UsernameToken) {
                        Document signedDoc = exchange.getIn().getBody(Document.class);
                        UsernameToken token = (UsernameToken) agreement.getSecurity().getSecurityToken();
                        RequestData requestData = new RequestData();
                        AttachmentCallbackHandler attachmentCallback = null;
                        if(exchange.getIn().hasAttachments()) {
                            attachmentCallback = createAttachmentCallbackHandler(exchange);
                            requestData.setAttachmentCallbackHandler(attachmentCallback);
                        }
                        requestData.setSigVerCrypto(crypto);
                        requestData.setDecCrypto(crypto);
                        requestData.setWssConfig(securityEngine.getWssConfig());
                        requestData.getWssConfig().setPasswordsAreEncoded(token.isDigest());
                        requestData.setAddUsernameTokenCreated(token.isCreated());
                        requestData.setAddUsernameTokenNonce(token.isNonce());
                        requestData.setCallbackHandler(userTokenCallbackHandler);
                        requestData.setUsername(token.getUsername());
                        List<WSSecurityEngineResult> results = securityEngine.processSecurityHeader(signedDoc, null, requestData);

                        if(results != null && results.size() > 0) {
                            exchange.getIn().setHeader(EbmsConstants.SECURITY_CHECK,results.get(0).get(WSSecurityEngineResult.TAG_VALIDATED_TOKEN));
                            if(exchange.getIn().hasAttachments() && attachmentCallback != null) {
                                exchange.getIn().setAttachments(attachmentCallback.getVerifiedAttachments());
                            }
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
                        builder.setUserInfo(token.getUsername(), token.getPassword());

                        WSSecHeader secHeader = new WSSecHeader();
                        secHeader.insertSecurityHeader(message);
                        Document signedDoc = builder.build(message, secHeader);

                        exchange.getIn().setBody(signedDoc);
                    }
                }
            })
            .log(LoggingLevel.INFO, "Signing for ${headers.JentrataCPAId} - on message:${headers.JentrataMessageID}")
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    Document message = exchange.getIn().getBody(Document.class);
                    PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                    if (agreement.requiresSignature()) {
                        Signature signatureAgreement = agreement.getSecurity().getSignature();
                        List<WSEncryptionPart> parts = new ArrayList<>();
                        for (SignaturePart signaturePart : signatureAgreement.getSignatureParts()) {
                            WSEncryptionPart part;
                            if (signaturePart.getNamespace() != null) {
                                part = new WSEncryptionPart(signaturePart.getElementName(), signaturePart.getNamespace(), signaturePart.getEncryptMethod());
                            } else {
                                part = new WSEncryptionPart(signaturePart.getElementName(),signaturePart.getEncryptMethod());
                            }
                            parts.add(part);
                        }
                        WSSecSignature signature = new WSSecSignature();
                        signature.setUserInfo(signatureAgreement.getKeyStoreAlias(), signatureAgreement.getKeyStorePass());
                        signature.setSignatureAlgorithm(signatureAgreement.getSignatureAlgorithm());
                        signature.setDigestAlgo(signatureAgreement.getSignatureHashFunction());
                        signature.setParts(parts);

                        AttachmentCallbackHandler attachmentCallbackHandler = createAttachmentCallbackHandler(exchange);
                        signature.setAttachmentCallbackHandler(attachmentCallbackHandler);
                        WSSecHeader secHeader = new WSSecHeader();
                        secHeader.insertSecurityHeader(message);
                        Document signedDoc = signature.build(message, crypto, secHeader);

                        exchange.getIn().setBody(signedDoc);
                        exchange.getIn().setAttachments(attachmentCallbackHandler.getVerifiedAttachments());
                    }
                }
            })
        .routeId("_jentrataWSSEInsertUsernameToken");
    }

    private AttachmentCallbackHandler createAttachmentCallbackHandler(Exchange exchange) throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        if(exchange.getIn().hasAttachments()) {
            Map<String,DataHandler> attachmentMap = exchange.getIn().getAttachments();
            for(Map.Entry<String,DataHandler> entry : attachmentMap.entrySet()) {
                Attachment attachment = new Attachment();
                attachment.setId(entry.getKey());
                attachment.setMimeType(entry.getValue().getContentType());
                attachment.setSourceStream(entry.getValue().getInputStream());
                attachments.add(attachment);
            }
        } else if(exchange.getIn().getHeader(EbmsConstants.MESSAGE_PAYLOADS) != null) {
            List<Map<String, Object>> payloads = (List<Map<String, Object>>) exchange.getIn().getHeader(EbmsConstants.MESSAGE_PAYLOADS);
            for(Map<String,Object> payload : payloads) {
                Attachment attachment = new Attachment();
                attachment.setId((String) payload.get("payloadId"));
                attachment.setMimeType((String) payload.get("contentType"));
                String content = (String) payload.get("content");
                attachment.setSourceStream(new ByteArrayInputStream(content.getBytes()));
                attachments.add(attachment);
            }

        }
        return new AttachmentCallbackHandler(attachments);
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

    public CallbackHandler getUserTokenCallbackHandler() {
        return userTokenCallbackHandler;
    }

    public void setUserTokenCallbackHandler(CallbackHandler userTokenCallbackHandler) {
        this.userTokenCallbackHandler = userTokenCallbackHandler;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }
}
