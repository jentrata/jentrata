package org.jentrata.ebms.cpa.pmode;

/**
 * Holds ebMS3 PMode Security configuration information
 *
 * @author aaronwalker
 */
public class Security {

    public static final Security DEFAULT_SECURITY = new Security();

    public enum ReplyPatternType {
        Callback,
        Response
    }

    private boolean sendReceipt = true;
    private ReplyPatternType sendReceiptReplyPattern = ReplyPatternType.Callback;
    private boolean sendReceiptNonRepudiation = false;
    private boolean disableBSPEnforcement = false;
    private boolean inclusiveNamespacesEnabled = true;
    private Signature signature = null;

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

    public boolean isDisableBSPEnforcement() {
        return disableBSPEnforcement;
    }

    public void setDisableBSPEnforcement(boolean disableBSPEnforcement) {
        this.disableBSPEnforcement = disableBSPEnforcement;
    }

    public boolean isInclusiveNamespacesEnabled() {
        return inclusiveNamespacesEnabled;
    }

    public void setInclusiveNamespacesEnabled(boolean inclusiveNamespacesEnabled) {
        this.inclusiveNamespacesEnabled = inclusiveNamespacesEnabled;
    }
}
