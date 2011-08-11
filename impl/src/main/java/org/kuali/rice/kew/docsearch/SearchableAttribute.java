/*
 * Copyright 2005-2007 The Kuali Foundation
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.kuali.rice.kew.docsearch;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kew.rule.WorkflowAttributeValidationError;
import org.kuali.rice.kns.web.ui.Row;


/**
 * SearchableAttribute must implement this interface
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface SearchableAttribute extends Serializable {

    public static final String SEARCH_WILDCARD_CHARACTER = "*";
    public static final String SEARCH_WILDCARD_CHARACTER_REGEX_ESCAPED = "\\" + SEARCH_WILDCARD_CHARACTER;

    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_DATE = "datetime";
    public static final String DATA_TYPE_LONG = "long";
    public static final String DATA_TYPE_FLOAT = "float";

    public static final String DEFAULT_SEARCHABLE_ATTRIBUTE_TYPE_NAME = DATA_TYPE_STRING;

    public static final String DEFAULT_RANGE_SEARCH_LOWER_BOUND_LABEL = "From";
    public static final String DEFAULT_RANGE_SEARCH_UPPER_BOUND_LABEL = "To";

    public static final String RANGE_LOWER_BOUND_PROPERTY_PREFIX = "rangeLowerBoundKeyPrefix_";
    public static final String RANGE_UPPER_BOUND_PROPERTY_PREFIX = "rangeUpperBoundKeyPrefix_";

    public static final List SEARCHABLE_ATTRIBUTE_BASE_CLASS_LIST = Arrays.asList(new Class[]{
	    SearchableAttributeStringValue.class, SearchableAttributeFloatValue.class, SearchableAttributeLongValue.class,
	    SearchableAttributeDateTimeValue.class});

    /**
     * this gives the xml representation of the attribute; returning a standard java xml object might be a better
     * approach here
     *
     * @return
     */
    public String getSearchContent(DocumentSearchContext documentSearchContext);

    /**
     * this will return the loaded data objects for storage in workflow�s database to be related to the document the
     * attributes xml content was loaded with
     *
     * @return
     */
    public List<SearchableAttributeValue> getSearchStorageValues(DocumentSearchContext documentSearchContext);

    /**
     * this will return a list of field objects to be rendered in the docsearch interface
     *
     * @return
     */
    public List<Row> getSearchingRows(DocumentSearchContext documentSearchContext);

    /**
     * this will return a list of error objects if the user has made an input error
     *
     * @return
     */
    public List<WorkflowAttributeValidationError> validateUserSearchInputs(
    		Map<Object, String> paramMap, DocumentSearchContext searchContext);
}
