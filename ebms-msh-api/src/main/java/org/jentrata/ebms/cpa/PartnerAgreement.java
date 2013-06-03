package org.jentrata.ebms.cpa;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jentrata.ebms.cpa.pmode.Security;

import java.util.List;

/**
 * A Agreement between 2 trading partners
 *
 * @author aaronwalker
 */
public class PartnerAgreement {

    private String cpaId;
    private boolean active = true;
    private String transportReceiverEndpoint;
    private List<Service> services;
    private Security security;


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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public boolean hasService(final String serviceName, final String action) {
        Iterable<Service> s = Iterables.filter(services, new Predicate<Service>() {
            @Override
            public boolean apply(Service service) {
                return service.getService().equals(serviceName) && service.getAction().equals(action);
            }
        });
        return s.iterator().hasNext();
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public boolean hasSecurityToken() {
        return security != null && security.getSecurityToken() != null;
    }

    public boolean requiresSignature() {
        return security != null && security.getSignature() != null;
    }
}
