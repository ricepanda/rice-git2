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
package org.kuali.rice.kim.bo.impl;

import java.util.LinkedHashMap;

import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kim.util.KIMPropertyConstants;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.Campus;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.bo.ParameterDetailType;
import org.kuali.rice.kns.bo.Namespace;
import org.kuali.rice.kns.bo.TransientBusinessObjectBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class KimAttributes extends TransientBusinessObjectBase {

	private static final long serialVersionUID = 8976113842166331719L;

	public static final String PRINCIPAL_ID = KIMPropertyConstants.Person.PRINCIPAL_ID;
	
	public static final String BUTTON_NAME = "buttonName";
	public static final String BEAN_NAME = "beanName";
	public static final String ACTION_CLASS = "actionClass";
	public static final String NAMESPACE_CODE = "namespaceCode";
	public static final String COMPONENT_NAME = "componentName";
	public static final String PROPERTY_NAME = "propertyName";
	public static final String EXISTING_RECORDS_ONLY = "existingRecordsOnly";
	public static final String CREATED_BY_SELF = "createdBySelf";
	public static final String CREATED_BY_SELF_ONLY = "createdBySelfOnly";
	public static final String ATTACHMENT_TYPE_CODE = "attachmentTypeCode";
	public static final String EDIT_MODE = "editMode";
	public static final String PARAMETER_NAME = "parameterName";
	public static final String CAMPUS_CODE = "campusCode";
	public static final String ACTION_REQUEST_CD = "actionRequestCd";
	public static final String ROUTE_STATUS_CODE = "routeStatusCode";
	public static final String ROUTE_NODE_NAME = "routeNodeName";
	public static final String ROLE_NAME = "roleName";
	public static final String PERMISSION_NAME = "permissionName";
	public static final String RESPONSIBILITY_NAME = "responsibilityName";
	public static final String GROUP_NAME = "groupName";
	public static final String REQUIRED = "required";
	public static final String ACTION_DETAILS_AT_ROLE_MEMBER_LEVEL = "actionDetailsAtRoleMemberLevel";
	public static final String DOCUMENT_NUMBER = "documentNumber";	
	public static final String DOCUMENT_TYPE_NAME = "documentTypeName";
	public static final String SECTION_ID = "sectionId";
	public static final String KIM_TYPE_ID = KimConstants.PrimaryKeyConstants.KIM_TYPE_ID;
	public static final String QUALIFIER_RESOLVER_PROVIDED_IDENTIFIER = "qualifierResolverProvidedIdentifier";
	
	protected String methodToCall;
	protected String beanName;
	protected String actionClass;
	protected String namespaceCode;
	protected String componentName;
	protected String propertyName;
	protected Boolean existingRecordsOnly;
	protected Boolean createdBySelfOnly;
	protected String attachmentTypeCode;
	protected String collectionItemTypeCode;
	protected String editMode;
	protected String parameterName;
	protected String campusCode;
	protected String documentTypeName;
	protected String actionRequestCd;
	protected String routeStatusCode;
	protected String routeNodeName;
	protected String roleName;
	protected String permissionName;
	protected String responsibilityName;
	protected String groupName;
	protected Boolean required;
	protected Boolean actionDetailsAtRoleMemberLevel;
	protected String documentNumber;
	protected String sectionId;
	protected String kimTypeId;
	protected String qualifierResolverProvidedIdentifier;
	
	protected Campus campus;
	protected Namespace parameterNamespace;
	protected ParameterDetailType parameterDetailType;
	protected Parameter parameter;
	protected DocumentType documentType;

	/**
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();
		return m;
	}

	/**
	 * @return the methodToCall
	 */
	public String getMethodToCall() {
		return this.methodToCall;
	}

	/**
	 * @param methodToCall
	 *            the methodToCall to set
	 */
	public void setMethodToCall(String methodToCall) {
		this.methodToCall = methodToCall;
	}

	/**
	 * @return the beanName
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * @param beanName
	 *            the beanName to set
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * @return the actionClass
	 */
	public String getActionClass() {
		return this.actionClass;
	}

	/**
	 * @param actionClass
	 *            the actionClass to set
	 */
	public void setActionClass(String actionClass) {
		this.actionClass = actionClass;
	}

	/**
	 * @return the namespaceCode
	 */
	public String getNamespaceCode() {
		return this.namespaceCode;
	}

	/**
	 * @param namespaceCode
	 *            the namespaceCode to set
	 */
	public void setNamespaceCode(String namespaceCode) {
		this.namespaceCode = namespaceCode;
	}

	/**
	 * @return the componentName
	 */
	public String getComponentName() {
		return this.componentName;
	}

	/**
	 * @param componentName
	 *            the componentName to set
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * @param propertyName
	 *            the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the collectionItemTypeCode
	 */
	public String getCollectionItemTypeCode() {
		return this.collectionItemTypeCode;
	}

	/**
	 * @param collectionItemTypeCode
	 *            the collectionItemTypeCode to set
	 */
	public void setCollectionItemTypeCode(String collectionItemTypeCode) {
		this.collectionItemTypeCode = collectionItemTypeCode;
	}

	/**
	 * @return the editMode
	 */
	public String getEditMode() {
		return this.editMode;
	}

	/**
	 * @param editMode
	 *            the editMode to set
	 */
	public void setEditMode(String editMode) {
		this.editMode = editMode;
	}

	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return this.parameterName;
	}

	/**
	 * @param parameterName
	 *            the parameterName to set
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * @return the campusCode
	 */
	public String getCampusCode() {
		return this.campusCode;
	}

	/**
	 * @param campusCode
	 *            the campusCode to set
	 */
	public void setCampusCode(String campusCode) {
		this.campusCode = campusCode;
	}

	/**
	 * @return the documentTypeName
	 */
	public String getDocumentTypeName() {
		return this.documentTypeName;
	}

	/**
	 * @param documentTypeName
	 *            the documentTypeName to set
	 */
	public void setDocumentTypeName(String documentTypeName) {
		this.documentTypeName = documentTypeName;
	}

	/**
	 * @return the actionRequestCd
	 */
	public String getActionRequestCd() {
		return this.actionRequestCd;
	}

	/**
	 * @param actionRequestCd
	 *            the actionRequestCd to set
	 */
	public void setActionRequestCd(String actionRequestCd) {
		this.actionRequestCd = actionRequestCd;
	}

	/**
	 * @return the routeStatusCode
	 */
	public String getRouteStatusCode() {
		return this.routeStatusCode;
	}

	/**
	 * @param routeStatusCode
	 *            the routeStatusCode to set
	 */
	public void setRouteStatusCode(String routeStatusCode) {
		this.routeStatusCode = routeStatusCode;
	}

	/**
	 * @return the routeNodeName
	 */
	public String getRouteNodeName() {
		return this.routeNodeName;
	}

	/**
	 * @param routeNodeName
	 *            the routeNodeName to set
	 */
	public void setRouteNodeName(String routeNodeName) {
		this.routeNodeName = routeNodeName;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return this.roleName;
	}

	/**
	 * @param roleName
	 *            the roleName to set
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * @return the permissionName
	 */
	public String getPermissionName() {
		return this.permissionName;
	}

	/**
	 * @param permissionName
	 *            the permissionName to set
	 */
	public void setPermissionName(String permissionName) {
		this.permissionName = permissionName;
	}

	/**
	 * @return the responsibilityName
	 */
	public String getResponsibilityName() {
		return this.responsibilityName;
	}

	/**
	 * @param responsibilityName
	 *            the responsibilityName to set
	 */
	public void setResponsibilityName(String responsibilityName) {
		this.responsibilityName = responsibilityName;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the documentNumber
	 */
	public String getDocumentNumber() {
		return this.documentNumber;
	}

	/**
	 * @param documentNumber
	 *            the documentNumber to set
	 */
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	/**
	 * @return the existingRecordsOnly
	 */
	public Boolean isExistingRecordsOnly() {
		return this.existingRecordsOnly;
	}

	/**
	 * @param existingRecordsOnly the existingRecordsOnly to set
	 */
	public void setExistingRecordsOnly(Boolean existingRecordsOnly) {
		this.existingRecordsOnly = existingRecordsOnly;
	}

	/**
	 * @return the createdBySelfOnly
	 */
	public Boolean isCreatedBySelfOnly() {
		return this.createdBySelfOnly;
	}

	/**
	 * @param createdBySelfOnly the createdBySelfOnly to set
	 */
	public void setCreatedBySelfOnly(Boolean createdBySelfOnly) {
		this.createdBySelfOnly = createdBySelfOnly;
	}

	/**
	 * @return the required
	 */
	public Boolean isRequired() {
		return this.required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

	/**
	 * @return the actionDetailsAtRoleMemberLevel
	 */
	public Boolean isActionDetailsAtRoleMemberLevel() {
		return this.actionDetailsAtRoleMemberLevel;
	}

	/**
	 * @param actionDetailsAtRoleMemberLevel the actionDetailsAtRoleMemberLevel to set
	 */
	public void setActionDetailsAtRoleMemberLevel(
			Boolean actionDetailsAtRoleMemberLevel) {
		this.actionDetailsAtRoleMemberLevel = actionDetailsAtRoleMemberLevel;
	}

	public String getSectionId() {
		return this.sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public Campus getCampus() {
		return this.campus;
	}

	public void setCampus(Campus campus) {
		this.campus = campus;
	}

	public Namespace getParameterNamespace() {
		return this.parameterNamespace;
	}

	public void setParameterNamespace(Namespace parameterNamespace) {
		this.parameterNamespace = parameterNamespace;
	}

	public ParameterDetailType getParameterDetailType() {
		return this.parameterDetailType;
	}

	public void setParameterDetailType(ParameterDetailType parameterDetailType) {
		this.parameterDetailType = parameterDetailType;
	}

	public Parameter getParameter() {
		return this.parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public DocumentType getDocumentType() {
		return this.documentType;
	}

	public void setDocumentType(DocumentType documentType) {
		this.documentType = documentType;
	}

	public String getKimTypeId() {
		return this.kimTypeId;
	}

	public void setKimTypeId(String kimTypeId) {
		this.kimTypeId = kimTypeId;
	}

	public String getQualifierResolverProvidedIdentifier() {
		return qualifierResolverProvidedIdentifier;
	}

	public void setQualifierResolverProvidedIdentifier(String qualifierResolverProvidedIdentifier) {
		this.qualifierResolverProvidedIdentifier = qualifierResolverProvidedIdentifier;
	}

	/**
	 * @return the attachmentTypeCode
	 */
	public String getAttachmentTypeCode() {
		return this.attachmentTypeCode;
	}

	/**
	 * @param attachmentTypeCode the attachmentTypeCode to set
	 */
	public void setAttachmentTypeCode(String attachmentTypeCode) {
		this.attachmentTypeCode = attachmentTypeCode;
	}
	
}
