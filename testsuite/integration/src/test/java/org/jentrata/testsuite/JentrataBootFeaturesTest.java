package org.jentrata.testsuite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 *
 *
 * @author aaronwalker
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class JentrataBootFeaturesTest extends JentrataTestSupport {

    @Test
    public void testJentrataBoot() throws Exception {
        assertFeatureInstalled("config");
        assertFeatureInstalled("ssh");
        assertFeatureInstalled("management");
        assertFeatureInstalled("camel");
        assertFeatureInstalled("activemq-broker");
        assertFeatureInstalled("camel-jms");
        assertFeatureInstalled("jentrata-cxf");
        assertFeatureInstalled("jentrata-as4-core");
    }

}
