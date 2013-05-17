package org.jentrata.ebms.cpa.pmode;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * A token used to configure a WSSE security token type
 *
 * @author aaronwalker
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="tokenType")
@JsonSubTypes({
        @Type(value=UsernameToken.class, name="UsernameToken")
    })
public interface SecurityToken {

    /**
     * Gets the token type
     *
     * @return the token type
     */
    String getTokenType();
}
