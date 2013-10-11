package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.EbmsError;
import org.jentrata.ebms.MessageType;
import org.jentrata.ebms.cpa.*;
import org.jentrata.ebms.cpa.pmode.Service;

import java.util.ArrayList;
import java.util.List;

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
                    .log(LoggingLevel.WARN, "failed to find a valid partner agreement for ${headers.JentrataMessageID} - Service:${headers.JentrataService} - action:${headers.JentrataAction}")
                    .throwException(new InvalidPartnerAgreementException("failed to find a valid partner agreement"))
                .otherwise()
                    .log(LoggingLevel.DEBUG,"found matching partner agreement for ${headers.JentrataMessageID} - Service:${headers.JentrataService} - action:${headers.JentrataAction}")
            .end()
            .choice()
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.USER_MESSAGE))
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            //preform service/action level validations
                            PartnerAgreement agreement = exchange.getIn().getHeader(EbmsConstants.CPA, PartnerAgreement.class);
                            String serviceName = exchange.getIn().getHeader(EbmsConstants.MESSAGE_SERVICE, String.class);
                            String action = exchange.getIn().getHeader(EbmsConstants.MESSAGE_ACTION, String.class);
                            List<ValidationError> validationErrors = new ArrayList<>();
                            if (agreement != null) {
                                Service service = agreement.getService(serviceName, action);
                                if (service != null && service.getValidations() != null) {
                                    for (ValidationPredicate predicate : service.getValidations()) {
                                        if (!predicate.matches(exchange)) {
                                            validationErrors.add(
                                                    new ValidationError.Builder()
                                                            .error(EbmsError.EBMS_0003)
                                                            .description(exchange.getIn().getHeader(EbmsConstants.VALIDATION_ERROR_DESC,String.class))
                                                            .refMessageID(exchange.getIn().getHeader(EbmsConstants.REF_TO_MESSAGE_ID,String.class))
                                                            .create()
                                            );
                                        }
                                    }
                                }
                            }
                            if(!validationErrors.isEmpty()) {
                                exchange.getIn().setHeader(EbmsConstants.EBMS_VALIDATION_ERROR,validationErrors);
                                throw new InvalidPartnerAgreementException(validationErrors);
                            } else {
                                exchange.getIn().setHeader(EbmsConstants.EBMS_VALIDATION_ERROR,null);
                                exchange.getIn().setHeader(EbmsConstants.VALIDATION_ERROR_DESC,null);
                            }
                        }
                    })
                .end()
            .end()
        .routeId("_jentrataValidatePartnerAgreement");

        from("direct:lookupCpaId")
            .to("direct:setDefaultCPAId")
            .choice()
                .when(header(EbmsConstants.CPA_ID).isNotEqualTo(null))
                    .setHeader(EbmsConstants.CPA, simple("bean:cpaRepository?method=findByCPAId"))
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.USER_MESSAGE))
                    .to("direct:lookupCpaIdByServiceAndAction")
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.SIGNAL_MESSAGE_WITH_USER_MESSAGE))
                    .to("direct:lookupCpaIdByRefMessageId")
                .when(header(EbmsConstants.MESSAGE_TYPE).isEqualTo(MessageType.SIGNAL_MESSAGE))
                    .to("direct:lookupCpaIdByRefMessageId")
                .otherwise()
                    .setHeader(EbmsConstants.CPA_ID,constant(EbmsConstants.CPA_ID_UNKNOWN))
        .routeId("_jentratalookupCpaId");

        from("direct:lookupCpaIdByServiceAndAction")
            .setHeader(EbmsConstants.CPA, simple("bean:cpaRepository?method=findByMessage"))
            .choice()
                .when(header(EbmsConstants.CPA).isNotEqualTo(null))
                    .setHeader(EbmsConstants.CPA_ID,simple("${headers.JentrataCPA.cpaId}"))
                .otherwise()
                    .setHeader(EbmsConstants.CPA_ID,constant(EbmsConstants.CPA_ID_UNKNOWN))
        .routeId("_jentrataLookupCpaIdByServiceAndAction");

        from("direct:lookupCpaIdByRefMessageId")
            .setHeader("JentrataOriginalMessageID",header(EbmsConstants.MESSAGE_ID))
            .setHeader(EbmsConstants.MESSAGE_ID,header(EbmsConstants.REF_TO_MESSAGE_ID))
            .setHeader("JentrataMessage", simple("bean:messageStore?method=findByMessageId"))
            .choice()
                .when(header("JentrataMessage").isNotEqualTo(null))
                    .setHeader(EbmsConstants.CPA_ID,simple("${headers.JentrataMessage.cpaId}"))
                    .setHeader(EbmsConstants.CPA, simple("bean:cpaRepository?method=findByCPAId"))
                .otherwise()
                    .to("direct:lookupCpaIdByServiceAndAction")
            .end()
            .setHeader(EbmsConstants.MESSAGE_ID,header("JentrataOriginalMessageID"))
        .routeId("_jentrataLookupCpaIdByRefMessageId");

        from("direct:setDefaultCPAId")
            .choice()
                .when(header(EbmsConstants.CPA_ID).isNull())
                    .setHeader(EbmsConstants.CPA_ID,header(EbmsConstants.DEFAULT_CPA_ID))
                .when(header(EbmsConstants.CPA_ID).isEqualTo(""))
                    .setHeader(EbmsConstants.CPA_ID,header(EbmsConstants.DEFAULT_CPA_ID))
                .routeId("_jentrataSetDefaultCPAId");
    }
}
