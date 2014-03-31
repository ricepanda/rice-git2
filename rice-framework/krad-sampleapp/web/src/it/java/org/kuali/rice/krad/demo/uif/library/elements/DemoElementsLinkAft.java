/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.demo.uif.library.elements;

import org.junit.Test;

import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DemoElementsLinkAft extends WebDriverLegacyITBase {

    /**
     * /kr-krad/kradsampleapp?viewId=Demo-LinkView&methodToCall=start
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=Demo-LinkView&methodToCall=start";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickById("Demo-LibraryLink", "");
        waitAndClickByLinkText("Elements");
        waitAndClickByLinkText("Link");
    }

    protected void testLibraryElementsLink() throws Exception {
        assertElementPresentByXpath("//section[@id='Demo-Link-Example1']/a[@target='_self']");
    }
    
    protected void testLibraryElementsCustomTarget() throws Exception {
        waitAndClickByLinkText("Custom Target");
        assertElementPresentByXpath("//section[@id='Demo-Link-Example2']/a[@target='_blank']");
    }
    
    protected void testLibraryElementsLinkUsingLightbox() throws Exception {
        waitAndClickByLinkText("Link using lightbox");
        assertElementPresentByXpath("//section[@id='Demo-Link-Example3']/a");
    }
    
    protected void testLibraryElementsLinkUsingBootstrapIcon() throws Exception {
        waitAndClickByLinkText("Link with a bootstrap icon");
        assertElementPresentByXpath("//section[@id='Demo-Link-Example4']/a[@class='uif-link uif-boxLayoutVerticalItem clearfix icon-pencil']");
    }
    
    @Test
    public void testElementsLinkBookmark() throws Exception {
        testLibraryElementsLink();
        testLibraryElementsCustomTarget();
        testLibraryElementsLinkUsingLightbox();
        testLibraryElementsLinkUsingBootstrapIcon();
        passed();
    }

    @Test
    public void testElementsLinkNav() throws Exception {
        testLibraryElementsLink();
        testLibraryElementsCustomTarget();
        testLibraryElementsLinkUsingLightbox();
        testLibraryElementsLinkUsingBootstrapIcon();
        passed();
    }  
}
