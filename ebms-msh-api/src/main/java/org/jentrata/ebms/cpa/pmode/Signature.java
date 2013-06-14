package org.jentrata.ebms.cpa.pmode;

import org.jentrata.ebms.EbmsConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information about signature verification
 *
 * @author aaronwalker
 */
public class Signature {

    public static final String RSA = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    public static final SignaturePart EBMS3_MESSAGE_PART = new SignaturePart("Messaging", EbmsConstants.EBXML_V3_NAMESPACE,"");

    private static final List<SignaturePart> DEFAULT_SIGNATURE_PARTS = new ArrayList<>();

    static {
        DEFAULT_SIGNATURE_PARTS.add(EBMS3_MESSAGE_PART);
        DEFAULT_SIGNATURE_PARTS.add(new SignaturePart("Body", EbmsConstants.SOAP_1_2_NAMESPACE,"Content"));
        DEFAULT_SIGNATURE_PARTS.add(new SignaturePart("cid:Attachments", null,"Content"));
    }

    private String signatureHashFunction = SHA256;
    private String signatureAlgorithm = RSA;
    private String keyStoreAlias = null;
    private String keyStorePass = null;
    private boolean encrypt = false;
    private List<SignaturePart> signatureParts = DEFAULT_SIGNATURE_PARTS;


    public String getSignatureHashFunction() {
        return signatureHashFunction;
    }

    public void setSignatureHashFunction(String signatureHashFunction) {
        this.signatureHashFunction = signatureHashFunction;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public void setKeyStoreAlias(String keyStoreAlias) {
        this.keyStoreAlias = keyStoreAlias;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public List<SignaturePart> getSignatureParts() {
        return signatureParts;
    }

    public void setSignatureParts(List<SignaturePart> signatureParts) {
        this.signatureParts = signatureParts;
    }
}
