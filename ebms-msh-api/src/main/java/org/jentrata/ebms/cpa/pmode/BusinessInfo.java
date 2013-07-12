package org.jentrata.ebms.cpa.pmode;

import java.util.Collections;
import java.util.List;

/**
 *  defines the business profile of a user message in terms of business header
 *  elements and their values (e.g. Service, Action) or other items with
 *  business significance (payload profile, MPC).
 *
 * @author aaronwalker
 */
public class BusinessInfo {

    public static final BusinessInfo DEFAULT = new BusinessInfo();
    static {
        DEFAULT.setServices(Collections.EMPTY_LIST);
        DEFAULT.setPayloadProfile(Collections.EMPTY_LIST);
        DEFAULT.setProperties(Collections.EMPTY_LIST);
    }

    private List<Service> services;
    private List<PayloadService> payloadProfile;
    private String mpc;
    private List<MessageProperty> properties;

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<PayloadService> getPayloadProfile() {
        return payloadProfile;
    }

    public void setPayloadProfile(List<PayloadService> payloadProfile) {
        this.payloadProfile = payloadProfile;
    }

    public String getMpc() {
        return mpc;
    }

    public void setMpc(String mpc) {
        this.mpc = mpc;
    }

    public List<MessageProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MessageProperty> properties) {
        this.properties = properties;
    }
}
