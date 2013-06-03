package org.jentrata.ebms.cpa.pmode;

/**
 * Holds ebMS3 PMode Security configuration information
 *
 * @author aaronwalker
 */
public class Security {

    public enum ReplyPatternType {
        Callback,
        Response
    }

    private SecurityToken securityToken = null;
    private boolean sendReceipt = true;
    private ReplyPatternType sendReceiptReplyPattern = ReplyPatternType.Callback;
    private boolean sendReceiptNonRepudiation = false;
    private Signature signature = null;


    public SecurityToken getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

    public boolean isSendReceipt() {
        return sendReceipt;
    }

    public void setSendReceipt(boolean sendReceipt) {
        this.sendReceipt = sendReceipt;
    }

    public ReplyPatternType getSendReceiptReplyPattern() {
        return sendReceiptReplyPattern;
    }

    public void setSendReceiptReplyPattern(ReplyPatternType sendReceiptReplyPattern) {
        this.sendReceiptReplyPattern = sendReceiptReplyPattern;
    }

    public boolean isSendReceiptNonRepudiation() {
        return sendReceiptNonRepudiation;
    }

    public void setSendReceiptNonRepudiation(boolean sendReceiptNonRepudiation) {
        this.sendReceiptNonRepudiation = sendReceiptNonRepudiation;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }
}
