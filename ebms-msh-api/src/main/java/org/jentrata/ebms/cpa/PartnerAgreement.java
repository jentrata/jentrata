package org.jentrata.ebms.cpa;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.pmode.BusinessInfo;
import org.jentrata.ebms.cpa.pmode.Party;
import org.jentrata.ebms.cpa.pmode.PayloadService;
import org.jentrata.ebms.cpa.pmode.Protocol;
import org.jentrata.ebms.cpa.pmode.ReceptionAwareness;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.cpa.pmode.Service;

/**
 * A Agreement between 2 trading partners
 *
 * @author aaronwalker
 */
public class PartnerAgreement {

    private String cpaId;
    private boolean active = true;

    private String agreementRef;
    private String mep = EbmsConstants.EBMS_V3_MEP_ONE_WAY;
    private String mepBinding = EbmsConstants.EBMS_V3_MEP_BINDING_PUSH;
    private Party initiator;
    private Party responder;
    private Protocol protocol;
    private BusinessInfo businessInfo = BusinessInfo.DEFAULT;
    private Security security = Security.DEFAULT_SECURITY;
    private ReceptionAwareness receptionAwareness = ReceptionAwareness.DEFAULT;

    public String getCpaId() {
        return cpaId;
    }

    public void setCpaId(String cpaId) {
        this.cpaId = cpaId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAgreementRef() {
        return agreementRef;
    }

    public void setAgreementRef(String agreementRef) {
        this.agreementRef = agreementRef;
    }

    public String getMep() {
        return mep;
    }

    public void setMep(String mep) {
        this.mep = mep;
    }

    public String getMepBinding() {
        return mepBinding;
    }

    public void setMepBinding(String mepBinding) {
        this.mepBinding = mepBinding;
    }

    public Party getInitiator() {
        return initiator;
    }

    public void setInitiator(Party initiator) {
        this.initiator = initiator;
    }

    public Party getResponder() {
        return responder;
    }

    public void setResponder(Party responder) {
        this.responder = responder;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public BusinessInfo getBusinessInfo() {
        return businessInfo;
    }

    public void setBusinessInfo(BusinessInfo businessInfo) {
        this.businessInfo = businessInfo;
    }

    public boolean hasService(final String serviceName, final String action) {
        Iterable<Service> s = Iterables.filter(businessInfo.getServices(), new Predicate<Service>() {
            @Override
            public boolean apply(Service service) {
                return service.getService().equals(serviceName) && service.getAction().equals(action);
            }
        });
        return s.iterator().hasNext();
    }

    public Service getService(String serviceName, String action) {
        for(Service service : businessInfo.getServices()) {
            if(service.getService().equals(serviceName) && service.getAction().equals(action)) {
                return service;
            }
        }
        return null;
    }

    public PayloadService getPayloadProfile(String payloadId) {
        if(businessInfo != null && businessInfo.getPayloadProfile() != null) {
            for(PayloadService payload : businessInfo.getPayloadProfile()) {
                if(payload.getPayloadId().equals(payloadId)) {
                    return payload;
                }
            }
        }
        return PayloadService.DEFAULT_PAYLOAD_SERVICE;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public ReceptionAwareness getReceptionAwareness() {
        return receptionAwareness;
    }

    public void setReceptionAwareness(ReceptionAwareness receptionAwareness) {
        this.receptionAwareness = receptionAwareness;
    }

    public boolean hasResponderSecurityToken() {
        return responder != null && responder.getAuthorization() != null;
    }

    public boolean hasInitiatorSecurityToken() {
        return initiator != null && initiator.getAuthorization() != null;
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
