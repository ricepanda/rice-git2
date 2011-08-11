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
package org.kuali.rice.kew.actionrequest.bo;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

/**
 * A values finder for returning KEW Action Request codes.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionRequestCodeValuesFinder extends KeyValuesBase {

	private static final List<KeyLabelPair> actionRequestCodes = new ArrayList<KeyLabelPair>();
	static {
		for (String actionRequestCode : KEWConstants.ACTION_REQUEST_CODES.keySet()) {
			actionRequestCodes.add(new KeyLabelPair(actionRequestCode, KEWConstants.ACTION_REQUEST_CODES.get(actionRequestCode)));
		}
	}
	
	/**
	 * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
	 */
	public List<KeyLabelPair> getKeyValues() {
		return actionRequestCodes;
	}

}
