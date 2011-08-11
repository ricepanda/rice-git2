/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.rice.kim.service.support.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.kim.bo.role.dto.RoleMembershipInfo;
import org.kuali.rice.kim.bo.types.dto.AttributeDefinitionMap;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.support.KimRoleTypeService;

public abstract class PassThruRoleTypeServiceBase implements KimRoleTypeService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PassThruRoleTypeServiceBase.class);
	
	public static final String UNMATCHABLE_QUALIFICATION = "!~!~!~!~!~";

    public abstract AttributeSet convertQualificationForMemberRoles(String namespaceCode, String roleName, String memberRoleNamespaceCode, String memberRoleName, AttributeSet qualification);
    
    public AttributeSet convertQualificationAttributesToRequired(AttributeSet qualificationAttributes) {
        return qualificationAttributes;
    }

    public List<RoleMembershipInfo> doRoleQualifiersMatchQualification(AttributeSet qualification, List<RoleMembershipInfo> roleMemberList) {
        return roleMemberList;
    }

    public boolean doesRoleQualifierMatchQualification(AttributeSet qualification, AttributeSet roleQualifier) {
        return true;
    }

    public List<RoleMembershipInfo> getRoleMembersFromApplicationRole(String namespaceCode, String roleName, AttributeSet qualification) {
        return new ArrayList<RoleMembershipInfo>(0);
    }
    
    public boolean hasApplicationRole(String principalId, List<String> groupIds, String namespaceCode, String roleName, AttributeSet qualification) {
        return false;
    }

    public boolean isApplicationRoleType() {
        return false;
    }

    public List<String> getAcceptedAttributeNames() {
        return new ArrayList<String>(0);
    }

    public AttributeDefinitionMap getAttributeDefinitions(String kimTypeId) {
        return null;
    }

//    public List<KeyLabelPair> getAttributeValidValues(String attributeName) {
//        return new ArrayList<KeyLabelPair>(0);
//    }

    public String getWorkflowDocumentTypeName() {
        return null;
    }
    
    /**
     * @see org.kuali.rice.kim.service.support.KimTypeService#getWorkflowRoutingAttributes(java.lang.String)
     */
    public List<String> getWorkflowRoutingAttributes(String routeLevel) {
    	return new ArrayList<String>(0);
    }

    public boolean supportsAttributes(List<String> attributeNames) {
        return true;
    }

    public AttributeSet translateInputAttributeSet(AttributeSet inputAttributeSet) {
        return inputAttributeSet;
    }

    public AttributeSet validateAttributes(String kimTypeId, AttributeSet attributes) {
        return null;
    }
    
    public List<RoleMembershipInfo> sortRoleMembers(List<RoleMembershipInfo> roleMembers) {
        return roleMembers;
    }
    
    

	/**
	 * This base implementation does nothing but log that the method was called.
	 * 
	 * @see org.kuali.rice.kim.service.support.KimRoleTypeService#principalInactivated(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void principalInactivated(String principalId, String namespaceCode,
			String roleName) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Principal Inactivated called: principalId="+principalId+" role=" + namespaceCode + "/" + roleName );
		}
		// base implementation - do nothing
	}

    public boolean validateUniqueAttributes(String kimTypeId, AttributeSet newAttributes, AttributeSet oldAttributes){
        return true;
    }

    public AttributeSet validateUnmodifiableAttributes(String kimTypeId, AttributeSet mainAttributes, AttributeSet delegationAttributes){
        return new AttributeSet();
    }
    
    public List<String> getUniqueAttributes(String kimTypeId){
        return new ArrayList<String>();
    }
    
	public AttributeSet validateAttributesAgainstExisting(String kimTypeId, AttributeSet newAttributes, AttributeSet oldAttributes){
		return new AttributeSet();
	}

}
