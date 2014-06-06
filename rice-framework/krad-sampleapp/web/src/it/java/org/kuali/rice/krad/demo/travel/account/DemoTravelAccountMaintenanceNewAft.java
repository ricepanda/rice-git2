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
package org.kuali.rice.krad.demo.travel.account;

import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;
import org.openqa.selenium.By;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DemoTravelAccountMaintenanceNewAft extends WebDriverLegacyITBase {

    /**
     * //div[@class='fancybox-item fancybox-close']
     */
    public static final String FANCY_BOX_CLOSE_XPATH = "//div[@class='fancybox-item fancybox-close']";
    
    /**
     * //div[@class='fancybox-item fancybox-close']
     */
    public static final String FANCY_BOX_IFRAME_XPATH = "//iframe[@class='fancybox-iframe']";

    /**
     * /kr-krad/maintenance?methodToCall=start&dataObjectClassName=org.kuali.rice.krad.demo.travel.dataobject.TravelAccount&hideReturnLink=true
     */
    public static final String BOOKMARK_URL = "/kr-krad/maintenance?methodToCall=start&dataObjectClassName=org.kuali.rice.krad.demo.travel.dataobject.TravelAccount&hideReturnLink=true";

    /**
     * Description field
     */
    public static final String DESCRIPTION_FIELD = "document.documentHeader.documentDescription";

    /**
     * Explanation field
     */
    public static final String EXPLANATION_FIELD = "document.documentHeader.explanation";

    /**
     * Organization document number field
     */
    public static final String ORGANIZATION_DOCUMENT_NUMBER_FIELD = "document.documentHeader.organizationDocumentNumber";

    /**
     * Travel account name field
     */
    public static final String TRAVEL_ACCOUNT_NAME_FIELD = "document.newMaintainableObject.dataObject.name";

    /**
     * Travel account nUMBER field
     */
    public static final String TRAVEL_ACCOUNT_NUMBER_FIELD = "document.newMaintainableObject.dataObject.number";

    /**
     * Travel account type code field
     */
    public static final String TRAVEL_ACCOUNT_TYPE_CODE_FIELD = "document.newMaintainableObject.dataObject.accountTypeCode";

    /**
     * Travel sub account field
     */
    public static final String SUB_ACCOUNT_FIELD_XPATH = "//div[@data-label='Travel Sub Account Number']/fieldset/input";

    /**
     * Travel sub account name field
     */
    public static final String SUB_ACCOUNT_NAME_FIELD_XPATH = "//div[@data-label='Sub Account Name']/input";

    /**
     * Subsidized percent
     */
    public static final String SUBSIDIZED_PERCENT_FIELD = "document.newMaintainableObject.dataObject.subsidizedPercent";

    /**
     * Date created.
     */
    public static final String DATE_CREATED_FIELD = "document.newMaintainableObject.dataObject.createDate";

    /**
     * Fiscal officer ID
     */
    public static final String FISCAL_OFFICER_ID_FIELD = "document.newMaintainableObject.dataObject.foId";

    @Override
    public String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    protected void navigate() throws Exception {
        waitAndClickById("Demo-DemoLink", "");
        waitAndClickByLinkText("Travel Account Maintenance (New)");
    }

    protected void testTravelAccountMaintenanceNew() throws Exception {
        waitAndTypeByName("document.documentHeader.documentDescription","Travel Account Maintenance New Test Document");
        String randomCode = RandomStringUtils.randomAlphabetic(9).toUpperCase();
        waitAndTypeByName("document.newMaintainableObject.dataObject.number",randomCode);
        waitAndTypeByName("document.newMaintainableObject.dataObject.name","Test Account Name");
        waitAndClickByXpath("icon-search link not found", "//a[@class='uif-actionLink icon-search']");
        gotoLightBox();
        waitAndClickButtonByText("Search");
        waitForElementNotPresent(By.xpath("//button[contains(text(),'Add New Line')]"));
        waitAndClickLinkContainingText("return value");
        clearTextByName("document.newMaintainableObject.dataObject.subsidizedPercent");
        waitAndClickButtonByText("submit");
        waitForTextPresent("Document was successfully submitted.");
    }

    protected void testTravelAccountMaintenanceEditXss() throws Exception {
    	checkForRequiredFields();
        waitAndTypeByName(DESCRIPTION_FIELD,"\"/><script>alert('!')</script>");
        waitAndTypeByName(EXPLANATION_FIELD,"\"/><script>alert('!')</script>");
        waitAndTypeByName(ORGANIZATION_DOCUMENT_NUMBER_FIELD,"\"/><script>alert('!')</script>");
        waitAndTypeByName(TRAVEL_ACCOUNT_NAME_FIELD,"Xss");
        waitAndTypeByName(TRAVEL_ACCOUNT_NUMBER_FIELD,"Xss");
        selectByName(TRAVEL_ACCOUNT_TYPE_CODE_FIELD,"Clearing Account Type");
        waitAndTypeByName("newCollectionLines['document.newMaintainableObject.dataObject.subAccounts'].subAccount","a1");
        waitAndTypeByXpath(SUB_ACCOUNT_NAME_FIELD_XPATH,"\"/><script>alert('!')</script>");
        waitAndTypeByName(SUBSIDIZED_PERCENT_FIELD,"\"/><script>alert('!')</script>");
//        waitAndTypeByName(DATE_CREATED_FIELD,"\"/><script>alert('!')</script>"); // no longer input field
        waitAndTypeByName(FISCAL_OFFICER_ID_FIELD,"\"/><script>alert('!')</script>");
        waitAndClickButtonByText("Save");
        Thread.sleep(1000);
        if(isAlertPresent())    {
            fail("XSS vulnerability identified.");
        }
    }
    
    private void checkForRequiredFields() throws Exception{
    	waitForElementPresentByXpath("//label[contains(text(),'Description')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Travel Account Number:')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Travel Account Name:')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Travel Account Type Code:')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Date Created:')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Travel Sub Account Number:')]/span[contains(text(),'*')]");
    	waitForElementPresentByXpath("//label[contains(text(),'Sub Account Name:')]/span[contains(text(),'*')]");
    	waitAndClickButtonByText("submit");
    	String requiredMessage []={"Description: Required","Travel Account Number: Required","Travel Account Name: Required","Travel Account Type Code: Required"};
    	assertTextPresent(requiredMessage);
        assertTrue(findElement(By.xpath("//h3[@id='pageValidationHeader']")).getText().contains("This page has"));
    	waitAndClickButtonByText("Save");
    	assertTextPresent(requiredMessage);
    	waitAndClickButtonByText("blanket approve");
    	assertTextPresent(requiredMessage);
    	waitAndClickButtonByText("add");
    	String addRequiredMessage [] ={"Travel Sub Account Number: Required","Sub Account Name: Required"};
    	assertTextPresent(addRequiredMessage);
    }

    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        }   // try
        catch (Exception Ex) {
            return false;
        }   // catch
    }

    @Test
    public void testDemoTravelAccountMaintenanceNewBookmark() throws Exception {
        testTravelAccountMaintenanceEditXss();
        testTravelAccountMaintenanceNew();
        passed();
    }

    @Test
    public void testDemoTravelAccountMaintenanceNewNav() throws Exception {
        testTravelAccountMaintenanceEditXss();
        testTravelAccountMaintenanceNew();
        passed();
    }
}
