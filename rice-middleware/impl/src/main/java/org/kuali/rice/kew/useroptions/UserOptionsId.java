/**
 * Copyright 2005-2013 The Kuali Foundation
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
package org.kuali.rice.kew.useroptions;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * This Compound Primary Class has been generated by the rice ojb2jpa Groovy script.  Please
 * note that there are no setter methods, only getters.  This is done purposefully as cpk classes
 * can not change after they have been created.  Also note they require a public no-arg constructor.
 * TODO: Implement the equals() and hashCode() methods. 
 */
public class UserOptionsId implements Serializable {

    private static final long serialVersionUID = -982957447172014416L;
    
    @Column(name="PRSN_OPTN_ID")
    private String optionId;
    @Column(name="PRNCPL_ID")
    private String workflowId;

    public UserOptionsId() {}

    public String getOptionId() { return optionId; }

    public String getWorkflowId() { return workflowId; }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UserOptionsId)) return false;
        if (o == null) return false;
        UserOptionsId pk = (UserOptionsId) o;
        return getOptionId() != null && getWorkflowId() != null && getOptionId().equals(pk.getOptionId()) && getWorkflowId().equals(pk.getWorkflowId());        
    }

    public int hashCode() {
		return (getOptionId() + getWorkflowId()).hashCode();    
	}

}

