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
package org.kuali.rice.kew.lookup.valuefinder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.web.KeyValue;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.util.GlobalVariables;

/**
 * This is a description of what this class does - chris don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class SavedSearchValuesFinder extends KeyValuesBase {

	/**
	 * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
	 */
	public List getKeyValues() {
		List<KeyLabelPair> savedSearchValues = new ArrayList<KeyLabelPair>();
		savedSearchValues.add(new KeyLabelPair("", "Searches"));
		savedSearchValues.add(new KeyLabelPair("*ignore*", "-----"));
		savedSearchValues.add(new KeyLabelPair("*ignore*", "-Named Searches"));
		List<KeyValue> namedSearches = KEWServiceLocator.getDocumentSearchService().getNamedSearches(GlobalVariables.getUserSession().getPrincipalId());
		for (KeyValue keyValue : namedSearches) {
			String label = StringUtils.abbreviate(keyValue.getValue(), 75);
			KeyLabelPair keyLabel = new KeyLabelPair(keyValue.getKey(),label);
			savedSearchValues.add(keyLabel);
		}
		savedSearchValues.add(new KeyLabelPair("*ignore*", "-----"));
		savedSearchValues.add(new KeyLabelPair("*ignore*", "-Recent Searches"));
		List<KeyValue> mostRecentSearches = KEWServiceLocator.getDocumentSearchService().getMostRecentSearches(GlobalVariables.getUserSession().getPrincipalId());
		for (KeyValue keyValue : mostRecentSearches) {
			String label = StringUtils.abbreviate(keyValue.getValue(), 75);
			KeyLabelPair keyLabel = new KeyLabelPair(keyValue.getKey(),label);
			savedSearchValues.add(keyLabel);
		}
		return savedSearchValues;
	}

}
