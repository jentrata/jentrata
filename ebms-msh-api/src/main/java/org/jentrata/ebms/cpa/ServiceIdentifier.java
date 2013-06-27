package org.jentrata.ebms.cpa;

/**
 * Used to Identifier a Service
 *
 * @author aaronwalker
 */
public class ServiceIdentifier {

    private String xpath;
    private String identifier;

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
