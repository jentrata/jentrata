package org.jentrata.ebms.cpa.internal;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public List<PartnerAgreement> getActivePartnerAgreements() {
        List<PartnerAgreement> active = new ArrayList<>();
        for(PartnerAgreement agreement : partnerAgreements) {
            if(agreement.isActive()) {
                active.add(agreement);
            }
        }
        return active;
    }

    public File getCpaJsonFile() {
        return cpaJsonFile;
    }

    public void setCpaJsonFile(File cpaJsonFile) {
        this.cpaJsonFile = cpaJsonFile;
    }
}
