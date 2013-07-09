package org.jentrata.ebms.cpa.validation;

import org.jentrata.ebms.utils.EbmsUtils;
import org.w3c.dom.Document;

/**
 *  An implementation of ValidationPredicate that executes an xpath expression
 *  against the message body and compares to the configured value and returns
 *  false if it doesn't match
 */
public class XPathConstantPredicate extends AbstractXPathPredicate {

    private String value;

    @Override
    boolean matches(Document body, String expression) throws Exception {
        String actual = EbmsUtils.ebmsXpathValue(body.getDocumentElement(),expression);
        return actual != null && actual.equals(value);
    }

    @Override
    protected String getValidationError() {
        return name + " does not equal excepted value of " + value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
