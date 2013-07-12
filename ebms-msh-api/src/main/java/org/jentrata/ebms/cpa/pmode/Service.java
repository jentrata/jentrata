package org.jentrata.ebms.cpa.pmode;

import org.jentrata.ebms.cpa.ValidationPredicate;

import java.util.List;

/**
 * A service agreement between trading partners
 *
 * @author aaronwalker
 */
public class Service {

    private String service;
    private String action;
    private ServiceIdentifier identifier = null;
    private List<ValidationPredicate> validations;

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

    public List<ValidationPredicate> getValidations() {
        return validations;
    }

    public void setValidations(List<ValidationPredicate> validations) {
        this.validations = validations;
    }
}
