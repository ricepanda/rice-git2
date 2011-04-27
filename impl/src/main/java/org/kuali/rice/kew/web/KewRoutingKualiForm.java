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
package org.kuali.rice.kew.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.service.WorkflowDocument;
import org.kuali.rice.kew.util.CodeTranslator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.services.KIMServiceLocator;
import org.kuali.rice.kns.web.struts.form.KualiForm;

/**
 * This is a description of what this class does - jjhanso don't forget to fill this in.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class KewRoutingKualiForm extends KualiForm {
    private static final long serialVersionUID = -3537002710069757806L;
    private WorkflowDocument workflowDocument;
    private Long docId;
    private String docTypeName;
    private String initiateURL;
    private String command;
    private String annotation;


    private boolean showBlanketApproveButton;
    protected Map appSpecificRouteActionRequestCds = new HashMap();
    // KULRICE-2924: Added appSpecificRouteRecipient2 to allow for better separation between person and group routing.
    protected AppSpecificRouteRecipient appSpecificRouteRecipient = new AppSpecificRouteRecipient();
    protected AppSpecificRouteRecipient appSpecificRouteRecipient2 = new AppSpecificRouteRecipient();
    protected List appSpecificRouteList = new ArrayList();

    protected String appSpecificRouteRecipientType = "person";
    // KULRICE-2924: Added appSpecificRouteActionRequestCd2 to allow for better separation between person and group routing.
    protected String appSpecificRouteActionRequestCd;
    protected String appSpecificRouteActionRequestCd2;
    protected Integer recipientIndex;
    protected String docHandlerReturnUrl;
    protected String removedAppSpecificRecipient;

    public void resetAppSpecificRoute(){
        appSpecificRouteRecipient = new AppSpecificRouteRecipient();
        appSpecificRouteRecipient2 = new AppSpecificRouteRecipient();
    }

    public Map getAppSpecificRouteActionRequestCds() {
        return appSpecificRouteActionRequestCds;
    }

    /**
     * @return Returns the initiateURL.
     */
    public String getInitiateURL() {
        return initiateURL;
    }
    /**
     * @param initiateURL The initiateURL to set.
     */
    public void setInitiateURL(String initiateURL) {
        this.initiateURL = initiateURL;
    }
    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command The command to set.
     */
    public void setCommand(String command) {
        this.command = command;
    }
    /**
     * @return Returns the annotation.
     */
    public String getAnnotation() {
        return annotation;
    }
    /**
     * @param annotation The annotation to set.
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    /**
     * @return Returns the showBlanketApproveButton.
     */
    public boolean isShowBlanketApproveButton() {
        return showBlanketApproveButton;
    }
    /**
     * @param showBlanketApproveButton The showBlanketApproveButton to set.
     */
    public void setShowBlanketApproveButton(boolean blanketApprove) {
        this.showBlanketApproveButton = blanketApprove;
    }
    /**
     * @return Returns the docId.
     */
    public Long getDocId() {
        return docId;
    }
    /**
     * @param docId The docId to set.
     */
    public void setDocId(Long docId) {
        this.docId = docId;
    }
    /**
     * @return Returns the workflowDocument.
     */
    public WorkflowDocument getWorkflowDocument() {
        return workflowDocument;
    }
    /**
     * @param workflowDocument The workflowDocument to set.
     */
    public void setWorkflowDocument(WorkflowDocument workflowDocument) {
        this.workflowDocument = workflowDocument;
    }

    /**
     * @return Returns the superUserSearch.
     */
    public boolean isSuperUserSearch() {
        return (command != null && command.equals(KEWConstants.SUPERUSER_COMMAND));
    }

    public String getDocTypeName() {
        return docTypeName;
    }

    public void setDocTypeName(String docTypeName) {
        this.docTypeName = docTypeName;
    }

    public void setAppSpecificPersonId(String networkId){
        if(networkId != null && !networkId.trim().equals("")){
            getAppSpecificRouteRecipient().setId(networkId);
        }
        getAppSpecificRouteRecipient().setType("person");
    }

    public void setAppSpecificWorkgroupId(String workgroupId){
        if(workgroupId != null){
            Group workgroup = KIMServiceLocator.getIdentityManagementService().getGroup(workgroupId);
            if(workgroup != null){
                getAppSpecificRouteRecipient2().setId(workgroup.getId());
            }
        }
        getAppSpecificRouteRecipient2().setType("workgroup");
    }

    public AppSpecificRouteRecipient getAppSpecificRouteRecipient() {
        return appSpecificRouteRecipient;
    }
    public void setAppSpecificRouteRecipient(AppSpecificRouteRecipient appSpecificRouteRecipient) {
        this.appSpecificRouteRecipient = appSpecificRouteRecipient;
    }
    
    public AppSpecificRouteRecipient getAppSpecificRouteRecipient2() {
        return appSpecificRouteRecipient2;
    }
    public void setAppSpecificRouteRecipient2(AppSpecificRouteRecipient appSpecificRouteRecipient2) {
        this.appSpecificRouteRecipient2 = appSpecificRouteRecipient2;
    }
    
    public List getAppSpecificRouteList() {
        return appSpecificRouteList;
    }
    public void setAppSpecificRouteList(List appSpecificRouteList) {
        this.appSpecificRouteList = appSpecificRouteList;
    }


    public void setAppSpecificRouteRecipientType(
            String appSpecificRouteRecipientType) {
        this.appSpecificRouteRecipientType = appSpecificRouteRecipientType;
    }
    public String getAppSpecificRouteRecipientType() {
        return appSpecificRouteRecipientType;
    }

    public AppSpecificRouteRecipient getAppSpecificRoute(int index) {
        while (getAppSpecificRouteList().size() <= index) {
            getAppSpecificRouteList().add(new AppSpecificRouteRecipient());
        }
        return (AppSpecificRouteRecipient) getAppSpecificRouteList().get(index);
    }


    public void setAppSpecificRoute(int index, AppSpecificRouteRecipient appSpecificRouteRecipient) {
        appSpecificRouteList.set(index, appSpecificRouteRecipient);
    }


    public String getAppSpecificRouteActionRequestCd() {
        return appSpecificRouteActionRequestCd;
    }
    public void setAppSpecificRouteActionRequestCd(
            String appSpecificRouteActionRequestCd) {
        this.appSpecificRouteActionRequestCd = appSpecificRouteActionRequestCd;
    }
    
    public String getAppSpecificRouteActionRequestCd2() {
        return appSpecificRouteActionRequestCd2;
    }
    public void setAppSpecificRouteActionRequestCd2(
            String appSpecificRouteActionRequestCd2) {
        this.appSpecificRouteActionRequestCd2 = appSpecificRouteActionRequestCd2;
    }
    
    public Integer getRecipientIndex() {
        return recipientIndex;
    }
    public void setRecipientIndex(Integer recipientIndex) {
        this.recipientIndex = recipientIndex;
    }


    public void establishVisibleActionRequestCds(){
        try {
            if(getWorkflowDocument() != null){
                Long docId = workflowDocument.getRouteHeaderId();
                DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(docId);
                DocumentType documentType = document.getDocumentType();
                boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService().canAdministerRouting(workflowDocument.getPrincipalId(), documentType);
                if (isSuperUser){
                	if (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved() || workflowDocument.stateIsEnroute()) {
                		appSpecificRouteActionRequestCds = CodeTranslator.arLabels;
                	}
                	else if (workflowDocument.stateIsProcessed() || workflowDocument.stateIsApproved() || workflowDocument.stateIsDisapproved()) {
                        appSpecificRouteActionRequestCds.clear();
                        appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL);
                        appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_FYI_REQ, KEWConstants.ACTION_REQUEST_FYI_REQ_LABEL);
                	}
                	else {
                        appSpecificRouteActionRequestCds.clear();
                        appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_FYI_REQ, KEWConstants.ACTION_REQUEST_FYI_REQ_LABEL);
                	}
                } else if(workflowDocument.isFYIRequested()){
                    appSpecificRouteActionRequestCds.clear();
                    appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_FYI_REQ, KEWConstants.ACTION_REQUEST_FYI_REQ_LABEL);
                } else if (workflowDocument.isAcknowledgeRequested()){
                    appSpecificRouteActionRequestCds.clear();
                    appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, KEWConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL);
                    appSpecificRouteActionRequestCds.put(KEWConstants.ACTION_REQUEST_FYI_REQ, KEWConstants.ACTION_REQUEST_FYI_REQ_LABEL);
                } else if(workflowDocument.isApprovalRequested() || workflowDocument.isCompletionRequested() || workflowDocument.stateIsInitiated()){
                    appSpecificRouteActionRequestCds = CodeTranslator.arLabels;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Caught exception building ad hoc action dropdown", e);
        }
    }
    public String getDocHandlerReturnUrl() {
        return docHandlerReturnUrl;
    }
    public void setDocHandlerReturnUrl(String docHandlerReturnUrl) {
        this.docHandlerReturnUrl = docHandlerReturnUrl;
    }

    public String getRemovedAppSpecificRecipient() {
        return removedAppSpecificRecipient;
    }
    public void setRemovedAppSpecificRecipient(
            String removedAppSpecificRecipient) {
        this.removedAppSpecificRecipient = removedAppSpecificRecipient;
    }
}
