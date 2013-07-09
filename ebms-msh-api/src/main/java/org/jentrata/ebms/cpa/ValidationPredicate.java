package org.jentrata.ebms.cpa;

import org.apache.camel.Exchange;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.jentrata.ebms.cpa.validation.XPathConstantPredicate;
import org.jentrata.ebms.cpa.validation.XPathPredicate;
import org.jentrata.ebms.cpa.validation.XPathRegexPredicate;

/**
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=XPathPredicate.class, name="xpath"),
        @JsonSubTypes.Type(value=XPathConstantPredicate.class, name="xpath_value"),
        @JsonSubTypes.Type(value=XPathRegexPredicate.class, name="xpath_regex")
})
public interface ValidationPredicate {

    /**
     * Evaluates the predicate on the message exchange and returns true if this
     * exchange matches the predicate
     *
     * @param exchange the message exchange
     * @return true if the predicate matches
     */
    boolean matches(Exchange exchange);
}
