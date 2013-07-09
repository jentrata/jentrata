package org.jentrata.ebms.cpa.validation;

import org.apache.camel.Exchange;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.utils.EbmsUtils;
import org.w3c.dom.Document;

/**
 *  An implementation of ValidationPredicate that executes an xpath expression against the message body
 */
public class XPathPredicate extends AbstractXPathPredicate {

    @Override
    boolean matches(Document body, String expression) throws Exception {
        return EbmsUtils.hasEbmsXpath(body,expression);
    }

    @Override
    protected String getValidationError() {
        return name + " is not a valid value for this agreement";
    }
}
