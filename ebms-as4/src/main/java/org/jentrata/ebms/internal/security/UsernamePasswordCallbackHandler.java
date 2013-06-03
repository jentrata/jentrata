package org.jentrata.ebms.internal.security;


import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Map;

/**
 * A Callback Handler implementation for the case of processing a Username Token
 * with an encoded password.
 *
 * @author aaronwalker
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler {

    private Map<String, String> users;

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for(Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                if(pc.getUsage() == WSPasswordCallback.Usage.USERNAME_TOKEN) {
                    if(users.containsKey(pc.getIdentifier())) {
                        pc.setPassword(users.get(pc.getIdentifier()));
                    } else {
                        throw new UnsupportedCallbackException(callback,"Invalid authentication details");
                    }
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }
}
