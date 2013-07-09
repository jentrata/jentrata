package org.jentrata.ebms.cpa.validation;

import org.jentrata.ebms.cpa.ValidationPredicate;

/**
 *
 */
public abstract class AbstractValidationPredicate implements ValidationPredicate {

    protected String name;
    protected String expression;

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
