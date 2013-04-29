package org.jentrata.ebms.cpa;

import java.util.List;

/**
 * A repository of CPA agreements
 *
 * @author aaronwalker
 */
public interface CPARepository {

    List<PartnerAgreement> getActivePartnerAgreements();
}
