package org.jentrata.ebms.cpa;

/**
 * A Agreement between 2 trading partners
 *
 * @author aaronwalker
 */
public class PartnerAgreement {

    private String cpaId;
    private boolean active = true;
    private String transportReceiverEndpoint;


    public String getCpaId() {
        return cpaId;
    }

    public void setCpaId(String cpaId) {
        this.cpaId = cpaId;
    }

    public String getTransportReceiverEndpoint() {
        return transportReceiverEndpoint;
    }

    public void setTransportReceiverEndpoint(String transportReceiverEndpoint) {
        this.transportReceiverEndpoint = transportReceiverEndpoint;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
