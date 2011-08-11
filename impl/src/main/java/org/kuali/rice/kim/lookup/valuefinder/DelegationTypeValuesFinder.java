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
package org.kuali.rice.kim.lookup.valuefinder;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

/**
 * A values finder for returning KEW rule delegation type codes.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DelegationTypeValuesFinder extends KeyValuesBase {

	private static final List<KeyLabelPair> delegationTypes = new ArrayList<KeyLabelPair>();
	static {
		delegationTypes.add(new KeyLabelPair("", ""));
		for (String delegationType : KimConstants.KimUIConstants.DELEGATION_TYPES.keySet()) {
			delegationTypes.add(new KeyLabelPair(delegationType, KimConstants.KimUIConstants.DELEGATION_TYPES.get(delegationType)));
		}
	}
	
	public List<KeyLabelPair> getKeyValues() {
		return delegationTypes;
	}

}
