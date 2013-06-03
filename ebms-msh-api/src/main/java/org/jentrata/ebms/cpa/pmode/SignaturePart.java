package org.jentrata.ebms.cpa.pmode;

/**
 * Holds information about a signature part
 *
 * @author aaronwalker
 */
public class SignaturePart {

    private String elementName;
    private String namespace;
    private String encryptMethod = "";

    public SignaturePart() {
    }

    public SignaturePart(String elementName, String namespace, String encryptMethod) {
        this.elementName = elementName;
        this.namespace = namespace;
        this.encryptMethod = encryptMethod;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEncryptMethod() {
        return encryptMethod;
    }

    public void setEncryptMethod(String encryptMethod) {
        this.encryptMethod = encryptMethod;
    }
}
