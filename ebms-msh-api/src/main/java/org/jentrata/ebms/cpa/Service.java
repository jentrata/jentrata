package org.jentrata.ebms.cpa;

/**
 * A service agreement between trading partners
 *
 * @author aaronwalker
 */
public class Service {

    private String service;
    private String action;
    private ServiceIdentifier identifier = null;

    public Service() {}

    public Service(String service, String action) {
        this.service = service;
        this.action = action;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public ServiceIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(ServiceIdentifier identifier) {
        this.identifier = identifier;
    }
}
