package org.jentrata.ebms.rest.internal.routes;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

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
                .when(body().isNotEqualTo(null))
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
            String body = exchange.getIn().getBody(String.class);
            Map<String,String> params = parseQueryString(exchange);
            String callback = params.get("callback");
            if (body != null && body.length() > 0) {
                Response r = Response.status(200)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(wrapJSONP(body, callback))
                        .build();
                exchange.getOut().setBody(r);
            } else {
                Response r = Response.status(404)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(wrapJSONP("{}", callback))
                        .build();
                exchange.getOut().setBody(r);
            }
        }

        private String wrapJSONP(String body, String callback) {
            if(callback == null) {
                return body;
            }
            return callback + "(" + body + ")";
        }

        private Map<String,String> parseQueryString(Exchange exchange) {
            Map<String,String> params  = new HashMap<String,String>();
            String queryString = exchange.getIn().getHeader("CamelHttpQuery",String.class);
            if(queryString != null) {
                String [] fields = queryString.split("&");
                for(String field : fields) {
                    String [] f = field.split("=");
                    if(f.length == 2) {
                        params.put(f[0],f[1]);
                    }
                }
            }
            return params;
        }
    }
}
