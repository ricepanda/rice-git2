/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kns.web.comparator;

import java.io.Serializable;
import java.util.Comparator;

public class StringValueComparator implements Serializable, Comparator {
    private static final StringValueComparator theInstance = new StringValueComparator();
    
    public StringValueComparator() {
    }

    public static StringValueComparator getInstance() {
        return theInstance;
    }
    
    public int compare(Object o1, Object o2) {
        // null guard. non-null value is greater. equal if both are null
        if (null == o1 || null == o2) {
            return (null == o1 && null == o2) ? 0 : ((null == o1) ? -1 : 1);
        }
        
        String s1 = (String) o1;
        String s2 = (String) o2;

        return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
    }
}
