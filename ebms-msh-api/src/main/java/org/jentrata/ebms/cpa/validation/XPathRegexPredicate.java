package org.jentrata.ebms.cpa.validation;

import org.jentrata.ebms.utils.EbmsUtils;
import org.w3c.dom.Document;

/**
 *  An implementation of ValidationPredicate that executes an xpath expression
 *  against the message body and executes the regex to see if the value matches
 */
public class XPathRegexPredicate extends AbstractXPathPredicate {

    private String regex;

    @Override
    boolean matches(Document body, String expression) throws Exception {
        String actual = EbmsUtils.ebmsXpathValue(body.getDocumentElement(), expression);
        return actual != null && actual.matches(regex);
    }

    @Override
    protected String getValidationError() {
        return name + " does not match pattern " + regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
