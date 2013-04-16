package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;

/**
 * Validates the incoming message to see if it conform to a current partner
 * agreement configured in Jentrata
 *
 * Currently this just assumes the partner is valid
 *
 * @author aaronwalker
 */
public class ValidatePartnerAgreementRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:validatePartner")
            .setHeader(EbmsConstants.VALID_PARTNER_AGREEMENT, constant(Boolean.TRUE))
            .setHeader(EbmsConstants.EBMS_MESSAGE_MEP, constant("One-Way"))
        .routeId("_jentrataValidatePartnerAgreement");
    }
}
