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
package org.kuali.rice.kim.bo.entity.dto;

import static org.kuali.rice.kim.bo.entity.dto.DtoUtils.unNullify;

import org.kuali.rice.kim.bo.entity.KimEntityAffiliation;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class KimEntityAffiliationInfo extends KimDefaultableInfo implements KimEntityAffiliation {

	private static final long serialVersionUID = 1L;

	protected String entityAffiliationId = "";
	protected String affiliationTypeCode = "";
	protected String campusCode = "";

	
	/**
	 * 
	 */
	public KimEntityAffiliationInfo() {
		super();
		active = true;
	}
	/**
	 * 
	 */
	public KimEntityAffiliationInfo( KimEntityAffiliation aff ) {
		this();
		if ( aff != null ) {
			entityAffiliationId = unNullify( aff.getEntityAffiliationId() );
			affiliationTypeCode = unNullify( aff.getAffiliationTypeCode() );
			campusCode = unNullify( aff.getCampusCode() );
			dflt = aff.isDefault();
			active = aff.isActive();
		}
	}
	/**
	 * @see org.kuali.rice.kim.bo.entity.KimEntityAffiliation#getAffiliationTypeCode()
	 */
	public String getAffiliationTypeCode() {
		return affiliationTypeCode;
	}

	/**
	 * @see org.kuali.rice.kim.bo.entity.KimEntityAffiliation#getCampusCode()
	 */
	public String getCampusCode() {
		return campusCode;
	}

	/**
	 * @see org.kuali.rice.kim.bo.entity.KimEntityAffiliation#getEntityAffiliationId()
	 */
	public String getEntityAffiliationId() {
		return entityAffiliationId;
	}

	public void setAffiliationTypeCode(String affiliationTypeCode) {
		this.affiliationTypeCode = affiliationTypeCode;
	}

	public void setCampusCode(String campusCode) {
		this.campusCode = campusCode;
	}

	public void setEntityAffiliationId(String entityAffiliationId) {
		this.entityAffiliationId = entityAffiliationId;
	}

}
