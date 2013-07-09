package org.jentrata.ebms.cpa.validation;

import org.apache.camel.Exchange;
import org.jentrata.ebms.EbmsConstants;
import org.w3c.dom.Document;

/**
 *
 */
public abstract class AbstractXPathPredicate extends AbstractValidationPredicate {

    @Override
    public boolean matches(Exchange exchange) {
        try {
            Document body = exchange.getIn().getBody(Document.class);
            if(matches(body,expression)) {
                exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,getValidationError());
                return true;
            }
        }
        catch (Exception ex) {
            exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,name + " validation failed:" + ex);
        }
        return false;
    }

    protected abstract String getValidationError();

    abstract boolean matches(Document body, String expression) throws Exception;

}
