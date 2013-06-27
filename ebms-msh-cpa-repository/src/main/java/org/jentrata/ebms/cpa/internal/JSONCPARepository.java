package org.jentrata.ebms.cpa.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.Service;
import org.jentrata.ebms.utils.EbmsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of CPARepository that loads them from a JSON config file
 *
 * @author aaronwalker
 */
public class JSONCPARepository implements CPARepository {

    private static final Logger LOG = LoggerFactory.getLogger(JSONCPARepository.class);

    private File cpaJsonFile;
    private List<PartnerAgreement> partnerAgreements = new ArrayList<>();

    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if(cpaJsonFile != null && cpaJsonFile.exists()) {
            partnerAgreements = mapper.readValue(cpaJsonFile, new TypeReference<List<PartnerAgreement>>(){});
            LOG.info("Loaded " + partnerAgreements.size() + " partner agreement(s) into the CPA Repository");
        } else {
            LOG.warn("unable to load CPA Repository from " + cpaJsonFile);
        }
    }

    @Override
    public List<PartnerAgreement> getPartnerAgreements() {
        return partnerAgreements;
    }

    @Override
    public List<PartnerAgreement> getActivePartnerAgreements() {
        List<PartnerAgreement> active = new ArrayList<>();
        for(PartnerAgreement agreement : partnerAgreements) {
            if(agreement.isActive()) {
                active.add(agreement);
            }
        }
        return active;
    }

    @Override
    public PartnerAgreement findByCPAId(String cpaId) {
        for(PartnerAgreement partnerAgreement : getActivePartnerAgreements()) {
            if(partnerAgreement.getCpaId().equals(cpaId)) {
                return partnerAgreement;
            }
        }
        return null;
    }

    @Override
    public PartnerAgreement findByServiceAndAction(final String service, final String action) {
        Iterable<PartnerAgreement> agreements = Iterables.filter(partnerAgreements,new Predicate<PartnerAgreement>() {
            @Override
            public boolean apply(PartnerAgreement partnerAgreement) {
                return partnerAgreement.isActive() && partnerAgreement.hasService(service,action);
            }
        });
        if(agreements.iterator().hasNext()) {
            return agreements.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public PartnerAgreement findByMessage(Document message, String ebmsVersion) {
        for(PartnerAgreement partnerAgreement : getActivePartnerAgreements()) {
            for(Service service :partnerAgreement.getServices()) {
                try {
                    if(serviceMatches(message,service)) {
                        return partnerAgreement;
                    }
                } catch (XPathExpressionException e) {
                    LOG.error("Error finding matching service for partner " + partnerAgreement.getCpaId() + ":" + e,e);
                }
            }
        }
        return null;
    }

    public boolean isValidPartnerAgreement(final Map<String, Object> fields) {
        String service = (String) fields.get(EbmsConstants.MESSAGE_SERVICE);
        String action = (String) fields.get(EbmsConstants.MESSAGE_ACTION);
        PartnerAgreement agreement = (PartnerAgreement) fields.get(EbmsConstants.CPA);
        if(agreement == null) {
            agreement = findByServiceAndAction(service,action);
        }
        return  agreement != null;
    }

    public File getCpaJsonFile() {
        return cpaJsonFile;
    }

    public void setCpaJsonFile(File cpaJsonFile) {
        this.cpaJsonFile = cpaJsonFile;
    }

    private boolean serviceMatches(Document ebms, Service service) throws XPathExpressionException {
        String serviceValue = EbmsUtils.ebmsXpathValue(ebms.getDocumentElement(),"//eb3:CollaborationInfo/eb3:Service/text()");
        String actionValue = EbmsUtils.ebmsXpathValue(ebms.getDocumentElement(),"//eb3:CollaborationInfo/eb3:Action/text()");
        boolean matches = service.getService().equals(serviceValue) && service.getAction().equals(actionValue);
        if(matches &&  service.getIdentifier() != null) {
            String value = EbmsUtils.ebmsXpathValue(ebms.getDocumentElement(),service.getIdentifier().getXpath());
            matches = service.getIdentifier().getIdentifier().equals(value);
        }
        return matches;
    }
}
