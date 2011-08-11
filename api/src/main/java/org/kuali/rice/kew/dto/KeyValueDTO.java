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
package org.kuali.rice.kew.dto;

import java.io.Serializable;

/**
 * This is a virtual object representing the KeyValue object.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class KeyValueDTO implements Serializable {

    private static final long serialVersionUID = 8488836176261012858L;

    private String key;
    private String value;
    private String userDisplayValue;

    public KeyValueDTO() {
        super();
    }

    public KeyValueDTO(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public KeyValueDTO(String key, String value, String userDisplayValue) {
    	this(key,value);
        this.userDisplayValue = userDisplayValue;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getUserDisplayValue() {
		return this.userDisplayValue;
	}

	public void setUserDisplayValue(String userDisplayValue) {
		this.userDisplayValue = userDisplayValue;
	}

}
