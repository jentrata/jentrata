package org.jentrata.ebms.cpa;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.pmode.PayloadService;
import org.jentrata.ebms.cpa.pmode.ReceptionAwareness;
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
    private Security security = Security.DEFAULT_SECURITY;
    private PayloadService payloadService = PayloadService.DEFAULT_PAYLOAD_SERVICE;
    private ReceptionAwareness receptionAwareness = ReceptionAwareness.DEFAULT;

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

    public Service getService(String serviceName, String action) {
        for(Service service : services) {
            if(service.getService().equals(serviceName) && service.getAction().equals(action)) {
                return service;
            }
        }
        return null;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public PayloadService getPayloadService() {
        return payloadService;
    }

    public void setPayloadService(PayloadService payloadService) {
        this.payloadService = payloadService;
    }

    public ReceptionAwareness getReceptionAwareness() {
        return receptionAwareness;
    }

    public void setReceptionAwareness(ReceptionAwareness receptionAwareness) {
        this.receptionAwareness = receptionAwareness;
    }

    public boolean hasSecurityToken() {
        return security != null && security.getSecurityToken() != null;
    }

    public boolean requiresSignature(MessageType messageType) {
        if(security != null && security.getSignature() != null) {
            switch (messageType) {
                case SIGNAL_MESSAGE:
                    return security.isSendReceiptNonRepudiation();
                case SIGNAL_MESSAGE_ERROR:
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }
}
