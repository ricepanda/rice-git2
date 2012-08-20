/**
 * Copyright 2005-2011 The Kuali Foundation
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
package edu.samplu.admin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.samplu.common.ITUtil;
import edu.samplu.common.UpgradedSeleniumITBase;
import org.junit.Test;

/**
 * tests that user 'admin', on blanket approving a new group maintenance document, results in a final document
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class IdentityGroupBlanketAppIT extends UpgradedSeleniumITBase {
    @Override
    public String getTestUrl() {
        return PORTAL;
    }

    @Test
    public void createIdentityGroupDoc() throws Exception {
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.click("link=Administration");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.click("link=Group");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.selectFrame("iframeportlet");
        selenium.click("//img[@alt='create new']");
        selenium.waitForPageToLoad("30000");
        String docId = selenium.getText("//div[@id='headerarea']/div/table/tbody/tr[1]/td[1]");
        selenium.type("//input[@id='document.documentHeader.documentDescription']", "Validation Test Group");
        selenium.select("//select[@id='document.groupNamespace']", IdentityResponsibilityBlanketAppIT.LABEL_KUALI_KUALI_SYSTEMS);
        selenium.waitForPageToLoad("30000");
        selenium.type("//input[@id='document.groupName']", "Validation Test Group1 " + ITUtil.DTS);
        selenium.click("methodToCall.performLookup.(!!org.kuali.rice.kim.impl.identity.PersonImpl!!).(((principalId:member.memberId,principalName:member.memberName))).((``)).((<>)).(([])).((**)).((^^)).((&&)).((//)).((~~)).(::::;;::::).anchorAssignees");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@name='methodToCall.search' and @value='search']");
        selenium.waitForPageToLoad("30000");
        selenium.click("link=return value");
        selenium.waitForPageToLoad("30000");
        selenium.click("methodToCall.addMember.anchorAssignees");
        selenium.waitForPageToLoad("30000");
        selenium.click("methodToCall.blanketApprove");
        selenium.waitForPageToLoad("30000");
        selenium.selectWindow("null");
        selenium.click("//img[@alt='doc search']");
        selenium.waitForPageToLoad("30000");
        assertEquals("Kuali Portal Index", selenium.getTitle());
        selenium.selectFrame("iframeportlet");
        selenium.click("//input[@name='methodToCall.search' and @value='search']");
        selenium.waitForPageToLoad("30000");
        docId= "link=" + docId;
        assertTrue(selenium.isElementPresent(docId));
        if(selenium.isElementPresent(docId)){            
            assertEquals("FINAL", selenium.getText("//table[@id='row']/tbody/tr[1]/td[4]"));
        }else{
            assertEquals(docId, selenium.getText("//table[@id='row']/tbody/tr[1]/td[1]"));
            assertEquals("FINAL", selenium.getText("//table[@id='row']/tbody/tr[1]/td[4]"));
        }
        
        
    }
}
