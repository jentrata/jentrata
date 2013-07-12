package org.jentrata.ebms.cpa.pmode;

/**
 * Holds information about an Initiator/Responder
 *
 * @author aaronwalker
 */
public class Party {

    private String partyId;
    private String partyIdType;
    private String role;
    private SecurityToken authorization;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getPartyIdType() {
        return partyIdType;
    }

    public void setPartyIdType(String partyIdType) {
        this.partyIdType = partyIdType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public SecurityToken getAuthorization() {
        return authorization;
    }

    public void setAuthorization(SecurityToken authorization) {
        this.authorization = authorization;
    }
}
