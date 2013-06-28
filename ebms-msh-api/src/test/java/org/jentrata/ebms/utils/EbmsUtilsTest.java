package org.jentrata.ebms.utils;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Unit test for org.jentrata.ebms.utils.EbmsUtils
 *
 * @author aaronwalker
 */
public class EbmsUtilsTest {

    @Test
    public void testExtractPartProperties() {

        assertThat(EbmsUtils.extractPartProperties(null), hasSize(0));
        assertThat(EbmsUtils.extractPartProperties(""), hasSize(0));
        assertThat(EbmsUtils.extractPartProperties(";"), hasSize(0));
        assertThat(EbmsUtils.extractPartProperties(";;"), hasSize(0));

        List<Map<String,Object>> actual = EbmsUtils.extractPartProperties("test;");
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), hasEntry("name",(Object)"test"));
        assertThat(actual.get(0), hasEntry("value",null));

        actual = EbmsUtils.extractPartProperties("test=test1;");
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), hasEntry("name",(Object)"test"));
        assertThat(actual.get(0), hasEntry("value",(Object)"test1"));

        actual = EbmsUtils.extractPartProperties("test=test1;test2=");
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), hasEntry("name",(Object)"test"));
        assertThat(actual.get(0), hasEntry("value",(Object)"test1"));
        assertThat(actual.get(1), hasEntry("name",(Object)"test2"));
        assertThat(actual.get(1), hasEntry("value",null));

        actual = EbmsUtils.extractPartProperties("test=;test2=test3");
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), hasEntry("name",(Object)"test"));
        assertThat(actual.get(0), hasEntry("value",null));
        assertThat(actual.get(1), hasEntry("name",(Object)"test2"));
        assertThat(actual.get(1), hasEntry("value",(Object)"test3"));

        actual = EbmsUtils.extractPartProperties("test=;test2=test3;");
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), hasEntry("name",(Object)"test"));
        assertThat(actual.get(0), hasEntry("value",null));
        assertThat(actual.get(1), hasEntry("name",(Object)"test2"));
        assertThat(actual.get(1), hasEntry("value",(Object)"test3"));
    }
}
