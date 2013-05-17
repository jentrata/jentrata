package org.jentrata.ebms.cpa.pmode;

/**
 * Holds WSSE UsernameToken details
 *
 * @author aaronwalker
 */
public class UsernameToken implements SecurityToken {

    private String username;
    private String password;
    private boolean digest = true;
    private boolean nonce = true;
    private boolean created = true;

    @Override
    public String getTokenType() {
        return getClass().getName();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDigest() {
        return digest;
    }

    public void setDigest(boolean digest) {
        this.digest = digest;
    }

    public boolean isNonce() {
        return nonce;
    }

    public void setNonce(boolean nonce) {
        this.nonce = nonce;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }
}
