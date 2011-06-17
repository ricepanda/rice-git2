/*
 * Copyright 2007 The Kuali Foundation
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

package org.kuali.rice.kns.bo;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;


/**
 *
 */
@Entity
@Table(name="KRNS_CMP_TYP_T")
public class CampusTypeImpl extends PersistableBusinessObjectBase implements Inactivateable, CampusType {

	@Id
	@Column(name="CAMPUS_TYP_CD")
	private String campusTypeCode;
	@Column(name="DOBJ_MAINT_CD_ACTV_IND")
	private boolean dataObjectMaintenanceCodeActiveIndicator;
	@Column(name="CMP_TYP_NM")
	private String campusTypeName;
	@Type(type="yes_no")
	@Column(name="ACTV_IND")
    protected boolean active;

	/**
	 * Default constructor.
	 */
	public CampusTypeImpl() {
        dataObjectMaintenanceCodeActiveIndicator = true;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#getCampusTypeCode()
	 */
	public String getCampusTypeCode() {
		return campusTypeCode;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#setCampusTypeCode(java.lang.String)
	 */
	public void setCampusTypeCode(String campusTypeCode) {
		this.campusTypeCode = campusTypeCode;
	}


	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#getDataObjectMaintenanceCodeActiveIndicator()
	 */
	public boolean getDataObjectMaintenanceCodeActiveIndicator() {
		return dataObjectMaintenanceCodeActiveIndicator;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#setDataObjectMaintenanceCodeActiveIndicator(boolean)
	 */
	public void setDataObjectMaintenanceCodeActiveIndicator(boolean dataObjectMaintenanceCodeActiveIndicator) {
		this.dataObjectMaintenanceCodeActiveIndicator = dataObjectMaintenanceCodeActiveIndicator;
	}


	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#getCampusTypeName()
	 */
	public String getCampusTypeName() {
		return campusTypeName;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#setCampusTypeName(java.lang.String)
	 */
	public void setCampusTypeName(String campusTypeName) {
		this.campusTypeName = campusTypeName;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#isActive()
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.kns.bo.CampusType#setActive(boolean)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	protected LinkedHashMap toStringMapper() {
	    LinkedHashMap m = new LinkedHashMap();
        m.put("campusTypeCode", this.campusTypeCode);
	    return m;
    }
}

