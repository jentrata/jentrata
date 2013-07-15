package org.jentrata.ebms.utils;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.jentrata.ebms.utils.ExpressionHelper.headerWithDefault;

/**
 * Unit test for ExpressionHelper
 *
 * @author aaronwalker
 */
public class ExpressionHelperTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:test")
    private MockEndpoint mockTest;

    @Test
    public void shouldGetDefaultValue() throws Exception {

        mockTest.setExpectedMessageCount(1);
        mockTest.expectedHeaderReceived("test","defaultValue");

        Exchange exchange = createExchangeWithBody("test");
        context().createProducerTemplate().send("direct:testExpressWithDefault",exchange);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void shouldGetDefaultSimpleValue() throws Exception {

        mockTest.setExpectedMessageCount(1);
        mockTest.expectedHeaderReceived("test",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        Exchange exchange = createExchangeWithBody("test");
        context().createProducerTemplate().send("direct:testExpressWithSimpleDefault",exchange);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void shouldGetHeaderValue() throws Exception {

        mockTest.setExpectedMessageCount(1);
        mockTest.expectedHeaderReceived("test","headerValue");

        Exchange exchange = createExchangeWithBody("test");
        exchange.getIn().setHeader("test","headerValue");
        context().createProducerTemplate().send("direct:testExpressWithDefault",exchange);
        assertMockEndpointsSatisfied();
    }


    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:testExpressWithDefault")
                    .setHeader("test", headerWithDefault("test",constant("defaultValue")))
                    .to(mockTest)
                .routeId("testExpressWithDefault");

                from("direct:testExpressWithSimpleDefault")
                    .setHeader("test", headerWithDefault("test",simple("${date:now:yyy-MM-dd}")))
                    .to(mockTest)
                .routeId("testExpressWithSimpleDefault");
            }
        };
    }
}
