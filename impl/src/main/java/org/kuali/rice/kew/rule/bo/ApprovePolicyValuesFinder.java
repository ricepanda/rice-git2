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
package org.kuali.rice.kew.rule.bo;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

/**
 * A values finder for returning KEW approve policy codes.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ApprovePolicyValuesFinder extends KeyValuesBase {

	private static final List<KeyLabelPair> approvePolicies = new ArrayList<KeyLabelPair>();
	static {
		for (String delegationType : KEWConstants.APPROVE_POLICIES.keySet()) {
			approvePolicies.add(new KeyLabelPair(delegationType, KEWConstants.APPROVE_POLICIES.get(delegationType)));
		}
	}
	
	public List<KeyLabelPair> getKeyValues() {
		return approvePolicies;
	}

}
