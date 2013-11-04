/**
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.testtools.common;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class PropertiesUtilsTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty("remote.driver.saucelabs", "true");
        System.setProperty("PROPERTY_DOESNT_EXIST", "true");

//        printSystemProperties();
    }

    private static void printSystemProperties() {
        Iterator iter = System.getProperties().stringPropertyNames().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            System.out.println(key + " = " + System.getProperty(key));
        }
    }

    @Ignore // TODO fix with mvn
    @Test
    public void testLoadPropertiesResource() throws IOException {
        Properties props = new PropertiesUtils().loadProperties(null, "JiraAwareRegexFailures.properties");
        Assert.assertTrue(props.keySet().size() > 0);
    }

    @Ignore // TODO address path
    @Test
    public void testLoadPropertiesFile() throws IOException {
        Properties props = new PropertiesUtils().loadProperties("rice-tools-test/src/main/resources/JiraAwareRegexFailures.properties", null);
        Assert.assertTrue(props.keySet().size() > 0);
    }

    @Test
    public void testLoadProperties() throws IOException {
        Properties props = new PropertiesUtils().loadProperties(null, "org/kuali/rice/testtools/common/PropertiesUtilsTest.properties");
        Assert.assertTrue(props.keySet().size() > 0);
        Assert.assertTrue(props.keySet().contains("remote.driver.saucelabs"));
        Assert.assertTrue(props.getProperty("remote.driver.saucelabs").equals(""));
    }

    @Test
    public void testSystemPropertiesOverrides() throws IOException {
        Assert.assertTrue(System.getProperty("remote.driver.saucelabs").equals("true"));
        Properties props = new PropertiesUtils().loadPropertiesWithSystemOverrides("org/kuali/rice/testtools/common/PropertiesUtilsTest.properties");
        Assert.assertTrue(props.keySet().size() > 0);
        Assert.assertTrue(props.getProperty("remote.driver.saucelabs").equals("true"));
        Assert.assertFalse(props.containsKey("PROPERTY_DOESNT_EXIST"));
    }

    @Test
    public void testSystemPropertiesAndOverrides() throws IOException {
        Assert.assertTrue(System.getProperty("remote.driver.saucelabs").equals("true"));
        Properties props = new PropertiesUtils().loadPropertiesWithSystemAndOverrides("org/kuali/rice/testtools/common/PropertiesUtilsTest.properties");
        Assert.assertTrue(props.keySet().size() > 0);
        Assert.assertTrue(props.getProperty("remote.driver.saucelabs").equals("true"));
        Assert.assertTrue(props.getProperty("PROPERTY_DOESNT_EXIST").equals("true"));
    }

    @Test
    public void testLoadPropertiesWithSystemAndOverridesIntoSystem() throws IOException {
        Assert.assertTrue(System.getProperty("remote.driver.saucelabs").equals("true"));
        Properties props = new PropertiesUtils().loadPropertiesWithSystemAndOverridesIntoSystem("org/kuali/rice/testtools/common/PropertiesUtilsTest.properties");
        Assert.assertTrue(System.getProperty("saucelabs.browser").equals("ff"));
    }
}
