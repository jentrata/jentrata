package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.InvalidPartnerAgreementException;

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
            .setHeader(EbmsConstants.EBMS_MESSAGE_MEP, constant("One-Way"))
            .setHeader(EbmsConstants.VALID_PARTNER_AGREEMENT, simple("bean:cpaRepository?method=isValidPartnerAgreement"))
            .choice()
                .when(header(EbmsConstants.VALID_PARTNER_AGREEMENT).isEqualTo(false))
                    .log(LoggingLevel.WARN,"failed to find a valid partner agreement for ${headers.JentrataMessageID} - Service:${headers.JentrataService} - action:${headers.JentrataAction}")
                    .throwException(new InvalidPartnerAgreementException("failed to find a valid partner agreement"))
                .otherwise()
                    .log(LoggingLevel.DEBUG,"found matching partner agreement for ${headers.JentrataMessageID} - Service:${headers.JentrataService} - action:${headers.JentrataAction}")
        .routeId("_jentrataValidatePartnerAgreement");

        from("direct:lookupCpaId")
            .choice()
                .when(header(EbmsConstants.CPA_ID).isNotEqualTo(null))
                    .setHeader(EbmsConstants.CPA, simple("bean:cpaRepository?method=findByCPAId"))
                .otherwise()
                    .setHeader(EbmsConstants.CPA, simple("bean:cpaRepository?method=findByServiceAndAction"))
                    .choice()
                        .when(header(EbmsConstants.CPA).isNotEqualTo(null))
                            .setHeader(EbmsConstants.CPA_ID,simple("${headers.JentrataCPA.cpaId}"))
                        .otherwise()
                            .setHeader(EbmsConstants.CPA_ID,constant(EbmsConstants.CPA_ID_UNKNOWN))
        .routeId("_jentratalookupCpaId");
    }
}
