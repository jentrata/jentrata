package org.jentrata.ebms.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.support.ExpressionAdapter;

/**
 * Camel Expression Helpers
 *
 * @author aaronwalker
 */
public class ExpressionHelper {

    /**
     * Creates an expression that gets the value of a header and if it is null
     * it evaluates a default expression
     *
     * @param name - header name
     * @param defaultExpression - default expression to evaluate if the header is null
     * @return an expression
     */
    public static final Expression headerWithDefault(final String name, final Expression defaultExpression) {
        return new ValueBuilder(new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                Object header = exchange.getIn().getHeader(name);
                if(header == null) {
                   return defaultExpression.evaluate(exchange,Object.class);
                } else {
                    return header;
                }
            }

            @Override
            public String toString() {
                return "headerWithDefault(" + name + ", " + defaultExpression + ")";
            }
        });
    }

}
