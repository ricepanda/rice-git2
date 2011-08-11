/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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
package org.kuali.rice.kew.engine.node;

import org.apache.commons.collections.KeyValue;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A simple object representing a key/value pair.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@MappedSuperclass
public class KeyValuePair implements KeyValue, Serializable {

	private static final long serialVersionUID = -3819394562029060331L;

	@Column(name="KEY_CD")
    protected String key;
    @Column(name="VAL")
    protected String value;

    public KeyValuePair() {}

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
