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
package edu.sampleu.kim.api.identity;

import org.junit.Test;
import org.kuali.rice.testtools.selenium.AutomatedFunctionalTestUtils;
import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;
import org.kuali.rice.testtools.selenium.WebDriverUtils;
import org.openqa.selenium.By;

/**
 * Sets up Person Roles for load-testing
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class IdentityPersonRoleAft extends WebDriverLegacyITBase {

    public static final String EDIT_URL = WebDriverUtils.getBaseUrlString() + "/kim/identityManagementPersonDocument.do?&principalId=LTID&docTypeName=IdentityManagementPersonDocument&methodToCall=docHandler&command=initiate";
    public static final String BOOKMARK_URL = AutomatedFunctionalTestUtils.PORTAL + "?channelTitle=Person&channelUrl=" + WebDriverUtils
            .getBaseUrlString() +
            "/kr/lookup.do?methodToCall=start&businessObjectClassName=org.kuali.rice.kim.api.identity.Person&docFormKey=88888888&returnLocation=" +
            AutomatedFunctionalTestUtils.PORTAL_URL + "&hideReturnLink=true";
    private int userCnt = Integer.valueOf(System.getProperty("test.role.user.cnt", "2")); // set to 176 for load testing
    private int userCntStart = Integer.valueOf(System.getProperty("test.role.user.cnt.start", "1"));  // set to 0 for load testing
    private String idBase = System.getProperty("test.role.user.base", "testadmin"); // set to lt for load testing
    public static final String ADMIN_ROLE_ID = "63";
    public static final String KRMS_ADMIN_ROLE_ID = "98";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Test
    public void testPersonRoleBookmark() throws InterruptedException {
        testPersonRole();
        passed();
    }

    private void testPersonRole() throws InterruptedException {
        String id = "";
        String format = "%0" + (userCnt + "").length() + "d";
        for(int i = userCntStart; i < userCnt; i++) {
            id = idBase + String.format(format, i);
            open(EDIT_URL.replace("LTID", id));
            waitAndTypeByName("document.documentHeader.documentDescription", "Admin permissions for " + id); // don't make unique

            selectByName("newAffln.affiliationTypeCode", "Affiliate");
            selectOptionByName("newAffln.campusCode", "BL");
            checkByName("newAffln.dflt");
            waitAndClickByName("methodToCall.addAffln.anchor");

            waitAndClick(By.id("tab-Membership-imageToggle"));
            waitAndType(By.id("newRole.roleId"), ADMIN_ROLE_ID);
            driver.findElement(By.name("methodToCall.addRole.anchor")).click();

            waitAndType(By.id("newRole.roleId"), KRMS_ADMIN_ROLE_ID);
            driver.findElement(By.name("methodToCall.addRole.anchor")).click();
            waitAndClickByName("methodToCall.blanketApprove");
            waitForPageToLoad();
        }
    }
}
