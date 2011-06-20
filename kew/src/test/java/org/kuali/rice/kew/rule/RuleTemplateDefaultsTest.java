/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.kew.rule;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kew.actionrequest.bo.RuleMaintenanceActionRequestCodeValuesFinder;
import org.kuali.rice.kew.document.RoutingRuleMaintainable;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.MaintenanceDocumentBase;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.web.struts.form.KualiForm;
import org.kuali.rice.kns.web.struts.form.KualiMaintenanceForm;

/**
 * This class tests the code that handles the default values for the rule templates.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RuleTemplateDefaultsTest extends KEWTestCase {

	/**
	 * Creates a KualiMaintenanceForm with the given rule template inside of its RuleBaseValues instance.
	 * 
	 * @param rtName The rule template to use.
	 */
	private void createNewKualiMaintenanceForm(String rtName) {
		// Initialize the required variables.
		final KualiMaintenanceForm kmForm = new KualiMaintenanceForm();
		final MaintenanceDocument maintDoc = new MaintenanceDocumentBase();
		final Maintainable oldMaint = new RoutingRuleMaintainable();
		final Maintainable newMaint = new RoutingRuleMaintainable();
		final RuleBaseValues rbValues = new RuleBaseValues();
		// Setup the rule base and the maintainables.
		rbValues.setRuleTemplate(KEWServiceLocator.getRuleTemplateService().findByRuleTemplateName(rtName));
		oldMaint.setBusinessObject(rbValues);
		oldMaint.setBoClass(rbValues.getClass());
		newMaint.setBusinessObject(rbValues);
		newMaint.setBoClass(rbValues.getClass());
		// Setup the maintenance document and the maintenance form.
		maintDoc.setOldMaintainableObject(oldMaint);
		maintDoc.setNewMaintainableObject(newMaint);
		maintDoc.getDocumentHeader().setDocumentDescription("This is a rule template test");
		kmForm.setDocument(maintDoc);
		GlobalVariables.setKualiForm(kmForm);
	}
	
	/**
	 * A convenience method for creating a set of expected key label pairs.
	 * 
	 * @param hasAcknowledge Indicates that a KeyLabelPair for "acknowledge" options should exist.
	 * @param hasComplete Indicates that a KeyLabelPair for "complete" options should exist.
	 * @param hasApprove Indicates that a KeyLabelPair for "approve" options should exist.
	 * @param hasFyi Indicates that a KeyLabelPair for "fyi" options should exist.
	 * @return A Set containing the desired expected KeyLabelPair keys.
	 */
	private Set<String> createExpectedKeysSet(boolean hasAcknowledge, boolean hasComplete, boolean hasApprove, boolean hasFyi) {
		final Set<String> expectedKeys = new HashSet<String>();
		// Insert the desired expected options into the set.
		if (hasAcknowledge) { expectedKeys.add(KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ); }
		if (hasComplete) { expectedKeys.add(KEWConstants.ACTION_REQUEST_COMPLETE_REQ); }
		if (hasApprove) { expectedKeys.add(KEWConstants.ACTION_REQUEST_APPROVE_REQ); }
		if (hasFyi) { expectedKeys.add(KEWConstants.ACTION_REQUEST_FYI_REQ); }
		return expectedKeys;
	}
	
	/**
	 * A convenience method for placing the keys from a KeyLabelPair list into a set.
	 * 
	 * @param kValues The KeyLabelPairs to process.
	 * @return A Set containing the keys of each KeyLabelPair.
	 */
	private Set<String> createSetOfKeyLabelPairKeys(List<KeyLabelPair> klpList) {
		final Set<String> actualKeys = new HashSet<String>();
		for (Iterator<KeyLabelPair> iterator = klpList.iterator(); iterator.hasNext();) {
			actualKeys.add((String) iterator.next().key);
		}
		return actualKeys;
	}
	
	/**
	 * Tests to ensure that the "TestRuleTemplate" in DefaultTestData.xml has the four action request options defined as "true",
	 * either explicitly or by default.
	 */
	@Test public void testAllTrueOptionsInTestRuleTemplate() throws Exception {
		createNewKualiMaintenanceForm("TestRuleTemplate");
		assertRuleTemplateHasExpectedKeyLabelPairs(
				createExpectedKeysSet(true, true, true, true),
				createSetOfKeyLabelPairKeys((new RuleMaintenanceActionRequestCodeValuesFinder()).getKeyValues()));
	}

	/**
	 * Tests to ensure that the proper key values are returned based upon the class type of the currently-set Kuali form.
	 * 
	 * @throws Exception
	 */
	@Test public void testCorrectKeyValuesReturnedBasedOnKualiFormInstance() throws Exception {
		// First, check that the proper values are returned when the Kuali form is *not* a KualiMaintenanceForm.
		GlobalVariables.setKualiForm(new KualiForm());
		assertRuleTemplateHasExpectedKeyLabelPairs(
				createExpectedKeysSet(true, true, true, true),
				createSetOfKeyLabelPairKeys((new RuleMaintenanceActionRequestCodeValuesFinder()).getKeyValues()));
		// Next, check that the proper values are returned when the Kuali form is a KualiMaintenanceForm containing a given rule template.
		loadXmlFile("RT_ValidRuleTemplatesWithVaryingDefaults.xml");
		createNewKualiMaintenanceForm("Test_Rule_Template2");
		assertRuleTemplateHasExpectedKeyLabelPairs(
				createExpectedKeysSet(false, false, false, true),
				createSetOfKeyLabelPairKeys((new RuleMaintenanceActionRequestCodeValuesFinder()).getKeyValues()));
	}
	
	/**
	 * Tests to ensure that the rule template in RT_ValidRuleTemplateWithFullDefaults.xml has the expected action request options.
	 * 
	 * @throws Exception
	 */
	@Test public void testOptionsInRT_ValidRuleTemplatesWithVaryingDefaults() throws Exception {
		loadXmlFile("RT_ValidRuleTemplatesWithVaryingDefaults.xml");
		final String[] ruleTemplates = {"RuleTemplate_With_Valid_Defaults", "RuleTemplate_With_More_Valid_Defaults"};
		final boolean[][] kSetBools = { {false, false, true, false}, {true, true, false, false} };
		final String[][] defaultActions = {
			{KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_APPROVE_REQ,KEWConstants.ACTION_REQUEST_APPROVE_REQ},
			{KEWConstants.ACTION_REQUEST_COMPLETE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ,KEWConstants.ACTION_REQUEST_COMPLETE_REQ}};
		// Test each rule template from the given file.
		for (int i = 0; i < ruleTemplates.length; i++) {
			createNewKualiMaintenanceForm(ruleTemplates[i]);
			assertRuleTemplateHasExpectedKeyLabelPairs(
					createExpectedKeysSet(kSetBools[i][0], kSetBools[i][1], kSetBools[i][2], kSetBools[i][3]),
					createSetOfKeyLabelPairKeys((new RuleMaintenanceActionRequestCodeValuesFinder()).getKeyValues()));
			assertRuleTemplateHasExpectedDefaultActions(defaultActions[i]);
		}
	}
	
	/**
	 * A convenience method for performing KeyLabelPair existence/nonexistence tests.
	 * 
	 * @param expectedKeys The expected KeyLabelPair keys.
	 * @param actualKeys The actual KeyLabelPair keys.
	 * @throws Exception
	 */
	private void assertRuleTemplateHasExpectedKeyLabelPairs(Set<String> expectedKeys, Set<String> actualKeys) throws Exception {
		// Check to see if all required keys are in the set.
		for (Iterator<String> iterator = expectedKeys.iterator(); iterator.hasNext();) {
			final String expKey = iterator.next();
			assertTrue("The key label pair with a key of '" + expKey + "' should have been true.", actualKeys.contains(expKey));
			actualKeys.remove(expKey);
		}
		// If any keys are still in the list, then fail the test because we expected their equivalent rule template options to
		// have a non-true value.
		if (!actualKeys.isEmpty()) {
			// Construct the error message.
			final String pluralStr = (actualKeys.size() != 1) ? "s" : "";
			final StringBuilder errMsg = new StringBuilder();
			errMsg.append("The key label pair").append(pluralStr).append(" with the key").append(pluralStr).append(" of ");
			for (Iterator<String> iterator = actualKeys.iterator(); iterator.hasNext();) {
				errMsg.append("'").append(iterator.next()).append(iterator.hasNext() ? "', " : "' ");
			}
			errMsg.append("should have been false.");
			// Fail the test.
			fail(errMsg.toString());
		}
	}
	
	/**
	 * A convenience method for verifying that a rule template contains the expected default action.
	 * 
	 * @param expectedDefActions The default actions expected by each responsibility (person, then group, then role).
	 * @throws Exception
	 */
	private void assertRuleTemplateHasExpectedDefaultActions(String[] expectedDefActions) throws Exception {
		// Acquire the Maintainable and the responsibility constants.
		final RoutingRuleMaintainable rrMaint = (RoutingRuleMaintainable) ((MaintenanceDocument) ((KualiMaintenanceForm)
				GlobalVariables.getKualiForm()).getDocument()).getNewMaintainableObject();
		final String[] respSectionConsts = { KEWPropertyConstants.PERSON_RESP_SECTION, KEWPropertyConstants.GROUP_RESP_SECTION,
				KEWPropertyConstants.ROLE_RESP_SECTION };
		// Check each responsibility's default action.
		for (int i = 0; i < respSectionConsts.length; i++) {
			final String actualDefAction =
					((RuleResponsibility) rrMaint.initNewCollectionLine(respSectionConsts[i])).getActionRequestedCd();
			assertEquals("The rule template does not have the expected default approve action.", expectedDefActions[i], actualDefAction);
		}
	}
}
