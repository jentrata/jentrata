package org.jentrata.ebms.rest.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.jentrata.ebms.EbmsConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides implementations of REST APIs exposed by Jentrata
 *
 * @author aaronwalker
 */
public class RestApiRouteBuilder extends RouteBuilder {

    private static final String CXF_RS_ENDPOINT_URI = "cxfrs://rest?resourceClasses=org.jentrata.ebms.rest.api.RepositoryService";

    private String restAPIEndpoint = CXF_RS_ENDPOINT_URI;

    @Override
    public void configure() throws Exception {
        from(restAPIEndpoint)
            .setExchangePattern(ExchangePattern.InOut)
            .removeHeader("accept-encoding")
            .process(new HttpPathProcessor())
            .recipientList(simple("direct:${header.serviceName}-${header.operationName}"))
            .choice()
                .when(header(EbmsConstants.CONTENT_TYPE).isEqualTo("application/json"))
                    .marshal().json(JsonLibrary.Jackson)
             .end()
            .process(new HttpResponseProcessor())
        .routeId("_jentrataRestApiImpl");

    }

    public String getRestAPIEndpoint() {
        return restAPIEndpoint;
    }

    public void setRestAPIEndpoint(String restAPIEndpoint) {
        this.restAPIEndpoint = restAPIEndpoint;
    }

    private class HttpPathProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String httpPath = exchange.getIn().getHeader("CamelHttpPath",String.class);
            String [] path = httpPath.split("/");
            if(path != null && path.length > 0) {
                exchange.getIn().setHeader("serviceName",path[1]);
            }
        }
    }

    private class HttpResponseProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            Object body = exchange.getIn().getBody();
            String contentType = exchange.getIn().getHeader(EbmsConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON, String.class);
            if(contentType.equals(MediaType.APPLICATION_JSON)) {
                String json = exchange.getIn().getBody(String.class);
                if(json.equals("null")) {
                    body = null;
                }
            }
            if (body != null) {
                Response r = Response.status(200)
                        .type(contentType)
                        .entity(body)
                        .build();
                exchange.getOut().setBody(r);
            } else {
                Response r = Response.status(404)
                        .type(contentType)
                        .entity(null)
                        .build();
                exchange.getOut().setBody(r);
            }
        }
    }
}
