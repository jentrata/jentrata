package org.jentrata.ebms.cpa.validation;

import org.apache.camel.Exchange;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.ValidationPredicate;
import org.jentrata.ebms.utils.EbmsUtils;
import org.w3c.dom.Document;

/**
 *  An implementation of ValidationPredicate that executes an xpath expression against the message body
 */
public class XPathPredicate implements ValidationPredicate {

    private String name;
    private String expression;

    @Override
    public boolean matches(Exchange exchange) {
        try {
            Document body = exchange.getIn().getBody(Document.class);
            if(EbmsUtils.hasEbmsXpath(body,expression)) {
                return true;
            }
            exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,name + " is not a valid value for this agreement");
        }
        catch (Exception ex) {
            exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,name + " validation failed:" + ex);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
