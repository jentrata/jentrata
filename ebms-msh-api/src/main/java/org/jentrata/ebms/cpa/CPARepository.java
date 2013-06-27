package org.jentrata.ebms.cpa;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.jentrata.ebms.EbmsConstants;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

/**
 * A repository of CPA agreements
 *
 * @author aaronwalker
 */
public interface CPARepository {

    /**
     * Gets all the Partner Agreements in the Repository
     *
     * @return all the configured partner agreements in the repository
     */
    List<PartnerAgreement> getPartnerAgreements();

    /**
     * Gets only the active Partner Agreements
     *
     * @return only the partner agreements that are marked as being active
     */
    List<PartnerAgreement> getActivePartnerAgreements();

    /**
     * Finds a active partner agreement by CPAId
     *
     * @param cpaId the service name
     * @return the partner agreement that has the matching CPAId
     */
    PartnerAgreement findByCPAId(@Header(EbmsConstants.CPA_ID) final String cpaId);

    /**
     * Finds a active partner agreement that has the service/action defined
     *
     * @param service the service name
     * @param action the corresponding service action
     * @return the partner agreement that has the service/action defined
     */
    PartnerAgreement findByServiceAndAction(@Header(EbmsConstants.MESSAGE_SERVICE) final String service, @Header(EbmsConstants.MESSAGE_ACTION) final String action);


    /**
     * Finds a active partner agreement given the ebms Messahe
     *
     * @param message the ebms message
     * @param ebmsVersion  the ebms message version
     * @return the first matching partner agreement
     */
    PartnerAgreement findByMessage(@Body Document message, @Header(EbmsConstants.EBMS_V3) String ebmsVersion);

    /**
     * Returns true if a valid partner agreements exists matching the
     * service/action combination contained the fields
     *
     * @param fields message header fields from the incoming message
     * @return
     */
    boolean isValidPartnerAgreement(@Headers final Map<String, Object> fields);

}
