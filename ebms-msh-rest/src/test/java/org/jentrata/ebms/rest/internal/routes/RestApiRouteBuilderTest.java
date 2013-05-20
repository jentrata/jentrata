package org.jentrata.ebms.rest.internal.routes;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.language.constant.ConstantLanguage;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.jentrata.ebms.EbmsConstants;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for org.jentrata.ebms.rest.internal.routes.RestApiRouteBuilder
 *
 * @author aaronwalker
 */
public class RestApiRouteBuilderTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:mockServiceImpl")
    protected MockEndpoint mockServiceImpl;

    @Test
    public void testCallServiceRouteImpl() throws Exception {
        mockServiceImpl.setExpectedMessageCount(1);
        mockServiceImpl.expectedHeaderReceived("serviceName", "testService");
        mockServiceImpl.expectedHeaderReceived("operationName", "testMethod");
        mockServiceImpl.returnReplyBody(ConstantLanguage.constant(
                new ImmutableMap.Builder<String,String>()
                    .put("key1", "value1")
                    .put("key2", "value2")
                    .build()
        ));
        Exchange request = new DefaultExchange(context);
        request.getIn().setBody("test");
        request.getIn().setHeader(Exchange.HTTP_PATH, "/testService/testMsgID");
        request.getIn().setHeader("operationName", "testMethod");
        Exchange response = context().createProducerTemplate().send("direct:testRestApi",request);

        assertMockEndpointsSatisfied();

        assertThat(response.getOut().getBody(),instanceOf(Response.class));
        Response r = response.getOut().getBody(Response.class);
        assertThat(r.getStatus(),equalTo(200));
        assertThat(new String((byte[])r.getEntity()),containsString("\"key1\":\"value1\""));
        assertThat(new String((byte[])r.getEntity()),containsString("\"key2\":\"value2\""));
    }

    @Test
    public void testCallServiceRouteImplWithEmptyResponse() throws Exception {
        mockServiceImpl.setExpectedMessageCount(1);
        mockServiceImpl.expectedHeaderReceived("serviceName", "testService");
        mockServiceImpl.expectedHeaderReceived("operationName", "testMethod");
        mockServiceImpl.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(null);
            }
        });

        Exchange request = new DefaultExchange(context);
        request.getIn().setBody("test");
        request.getIn().setHeader(Exchange.HTTP_PATH, "/testService/testMsgID");
        request.getIn().setHeader("operationName", "testMethod");
        Exchange response = context().createProducerTemplate().send("direct:testRestApi",request);

        assertMockEndpointsSatisfied();

        assertThat(response.getOut().getBody(),instanceOf(Response.class));
        Response r = response.getOut().getBody(Response.class);
        assertThat(r.getStatus(),equalTo(404));
        assertThat(r.getEntity(),nullValue());
    }

    @Override
    protected RouteBuilder[] createRouteBuilders() throws Exception {
        RestApiRouteBuilder routeBuilder = new RestApiRouteBuilder();
        routeBuilder.setRestAPIEndpoint("direct:testRestApi");
        return new RouteBuilder[] {
            routeBuilder,
            new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:testService-testMethod")
                        .setHeader(EbmsConstants.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
                        .to(mockServiceImpl)
                    .routeId("mockTestServiceImpl");
                }
            }
        };
    }
}
