package org.jentrata.ebms.as4.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.jentrata.ebms.cpa.CPARepository;
import org.jentrata.ebms.cpa.PartnerAgreement;

/**
 * Setups and outbound route per trading partner
 *
 * @author aaronwalker
 */
public class EbmsOutboundRouteBuilder extends RouteBuilder {

    private String outboundEbmsQueue = "activemq:queue:jentrata_internal_ebms_outbound";
    private CPARepository cpaRepository;

    @Override
    public void configure() throws Exception {
        from(outboundEbmsQueue)
            .convertBodyTo(String.class)
            .recipientList(simple("direct:outbox_${headers.JentrataCPAId}"))
        .routeId("_jentrataEbmsOutbound");

        for(PartnerAgreement agreement : cpaRepository.getActivePartnerAgreements()) {
            from("direct:outbox_" + agreement.getCpaId())
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to(configureEndpoint(agreement.getTransportReceiverEndpoint()))
                .choice()
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                        .to("direct:processSuccess")
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(204))
                        .to("direct:processSuccess")
                    .otherwise()
                        .to("direct:processFailure")
            .routeId("_jentrataEbmsOutbound" + agreement.getCpaId());
        }

        from("direct:processSuccess")
            .log(LoggingLevel.INFO,"Successfully delivered cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessageId} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG,"responseCode:${headers.CamelHttpResponseCode}\n${body}")
            //TODO: mark the message in the message store as delivered
        .routeId("_jentrataEbmsOutboundSuccess");

        from("direct:processFailure")
            .log(LoggingLevel.ERROR,"Failed to deliver cpaId:${headers.JentrataCPAId} - type:${headers.JentrataMessageType} - msgId:${headers.JentrataMessgeId} - responseCode:${headers.CamelHttpResponseCode}")
            .log(LoggingLevel.DEBUG,"responseCode:${headers.CamelHttpResponseCode}\n${body}")
            //TODO: mark the message in the message store as failed
        .routeId("_jentrataEbmsOutboundFailure");

    }

    public String getOutboundEbmsQueue() {
        return outboundEbmsQueue;
    }

    public void setOutboundEbmsQueue(String outboundEbmsQueue) {
        this.outboundEbmsQueue = outboundEbmsQueue;
    }

    public CPARepository getCpaRepository() {
        return cpaRepository;
    }

    public void setCpaRepository(CPARepository cpaRepository) {
        this.cpaRepository = cpaRepository;
    }

    private String configureEndpoint(String endpoint) {
        if(endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint + "?" + configureOptions();
        } else {
            return endpoint;
        }
    }

    protected String configureOptions() {
        return "throwExceptionOnFailure=false";
    }
}
