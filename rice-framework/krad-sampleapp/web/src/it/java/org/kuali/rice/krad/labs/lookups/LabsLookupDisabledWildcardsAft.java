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
package org.kuali.rice.krad.labs.lookups;

import org.junit.Test;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LabsLookupDisabledWildcardsAft extends LabsLookupBase {

    /**
     * /kr-krad/lookup?methodToCall=start&viewId=LabsLookup-DisabledWildcardsView&hideReturnLink=true
     */
    public static final String BOOKMARK_URL = "/kr-krad/lookup?methodToCall=start&viewId=LabsLookup-DisabledWildcardsView&hideReturnLink=true";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        navigateToLookup("Lookup Disabled Wildcards");
    }

    @Test
    public void testLabsLookupCriteriaFooterBookmark() throws Exception {
        testLabsLookupCriteriaFooter();
        passed();
    }

    @Test
    public void testLabsLookupCriteriaFooterNav() throws Exception {
        testLabsLookupCriteriaFooter();
        passed();
    }
    
    protected void testLabsLookupCriteriaFooter()throws Exception {
        waitAndTypeByName("lookupCriteria[number]","a*");
        waitAndClickButtonByText("Search");
        Thread.sleep(2000);
        assertTextPresent("No values match this search.");
        clearTextByName("lookupCriteria[number]");
        waitAndTypeByName("lookupCriteria[number]","a1");
        waitAndClickButtonByText("Search");
        Thread.sleep(3000);
        assertTextPresent("a1");
    }
}
