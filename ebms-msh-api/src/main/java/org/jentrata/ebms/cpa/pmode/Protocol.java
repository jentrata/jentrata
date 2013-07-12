package org.jentrata.ebms.cpa.pmode;

import org.jentrata.ebms.EbmsConstants;

/**
 * Represents the address and soap version (endpoint URL) of the Receiver MSH (or Receiver Party) to which Messages
 *
 * @author aaronwalker
 */
public class Protocol {

    private String address;
    private String soapVersion = EbmsConstants.SOAP_VERSION_1_2;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }
}
