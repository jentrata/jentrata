package org.jentrata.ebms.cpa;

/**
 * A Agreement between 2 trading partners
 *
 * @author aaronwalker
 */
public class PartnerAgreement {
    private String cpaId;
    private String transportReceiverEndpoint;

    public String getCPAId() {
        return cpaId;
    }

    public void setCPAId(String cpaId) {
        this.cpaId = cpaId;
    }

    public String getTransportReceiverEndpoint() {
        return transportReceiverEndpoint;
    }

    public void setTransportReceiverEndpoint(String transportReceiverEndpoint) {
        this.transportReceiverEndpoint = transportReceiverEndpoint;
    }
}
