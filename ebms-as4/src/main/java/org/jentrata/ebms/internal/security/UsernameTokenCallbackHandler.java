package org.jentrata.ebms.internal.security;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.jentrata.ebms.cpa.pmode.UsernameToken;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * A Callback Handler implementation for the case of processing a Username Token
 * with an password.
 *
 * @author aaronwalker
 */
public class UsernameTokenCallbackHandler implements CallbackHandler {

    private UsernameToken usernameToken;

    public UsernameTokenCallbackHandler(UsernameToken usernameToken) {
        this.usernameToken = usernameToken;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                if(pc.getUsage() == WSPasswordCallback.Usage.USERNAME_TOKEN) {
                    if(usernameToken.getUsername().equals(pc.getIdentifier())) {
                        pc.setPassword(usernameToken.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callback,"Invalid authentication details");
                    }
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }
}
