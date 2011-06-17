/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.rice.kew.service;

import java.io.Serializable;

import org.kuali.rice.kew.exception.WorkflowException;


public class WorkflowReports implements Serializable {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(WorkflowReports.class);
    
	private static final long serialVersionUID = 2859218832130678115L;
	public WorkflowInfo workflowInfo;
	
	public WorkflowReports() {
		workflowInfo = new WorkflowInfo();
	}
	
    public boolean isUserAuthenticatedByRouteLog(Long routeHeaderId, String principalId, boolean lookFuture) throws WorkflowException {
        return workflowInfo.isUserAuthenticatedByRouteLog(routeHeaderId, principalId, lookFuture);
    }
    
    /**
     * @deprecated use isLastApproverAtNode instead
     */
    public boolean isLastApproverInRouteLevel(Long routeHeaderId, String principalId, Integer routeLevel) throws WorkflowException {
        return workflowInfo.isLastApproverInRouteLevel(routeHeaderId, principalId, routeLevel);
    }
    
    public boolean isLastApproverAtNode(Long routeHeaderId, String principalId, String nodeName) throws WorkflowException {
        return workflowInfo.isLastApproverAtNode(routeHeaderId, principalId, nodeName);
    }
    
    /**
     * @deprecated use routeNodeHasApproverActionRequest instead
     */
    public boolean routeLevelHasApproverActionRequest(String docType, String docContent, Integer routeLevel) throws WorkflowException {
        return workflowInfo.routeLevelHasApproverActionRequest(docType, docContent, routeLevel);
    }
    
    public boolean routeNodeHasApproverActionRequest(String docType, String docContent, String nodeName) throws WorkflowException {
        return workflowInfo.routeNodeHasApproverActionRequest(docType, docContent, nodeName);
    }
    
    /**
     * User is considered the final approver for this document.
     * 
     * @return True if user context applies to the final approver request for this document.
     */
    public boolean isFinalApprover(Long routeHeaderId, String principalId) throws WorkflowException {
        return workflowInfo.isFinalApprover(routeHeaderId, principalId);
    }
    
    /*public boolean isUserAuthenticatedByRouteLog(Long routeHeaderId, UserIdVO userId, boolean lookFuture) throws WorkflowException {
    	ReportCriteriaDTO criteria = new ReportCriteriaDTO(routeHeaderId);
    	criteria.setUsersToFilterIn(new UserIdVO[] { userId });
    	RouteHeaderDetailVO detail = workflowInfo.routingReport(criteria);
    	if (isUser(detail.getInitiator(), userId) || detail.getActionsTaken().length > 0) {
    		return true;
    	}
    	lookFuture = lookFuture && new Boolean(Utilities.getApplicationConstant(KEWConstants.CHECK_ROUTE_LOG_AUTH_FUTURE)).booleanValue();
    	for (int index = 0; index < detail.getActionRequests().length; index++) {
    		ActionRequestVO actionRequest = detail.getActionRequests()[index];
    		if (actionRequest.getRouteLevel().intValue() > detail.getDocRouteLevel().intValue() && ! lookFuture) {
    			continue;
    		}
    		return true;
    	}
    	return false;
    }
    
    public boolean isLastApproverInRouteLevel(Long routeHeaderId, UserIdVO userId, Integer routeLevel) throws WorkflowException {
    	ReportCriteriaDTO criteria = new ReportCriteriaDTO(routeHeaderId, new Integer(0), routeLevel);
    	RouteHeaderDetailVO detail = workflowInfo.routingReport(criteria);
    	ActionTakenVO actionTaken = new ActionTakenVO();
    	actionTaken.setActionTaken(KEWConstants.ACTION_TAKEN_APPROVED_CD);
    	actionTaken.setRouteHeaderId(routeHeaderId);
    	actionTaken.setUserVO(workflowInfo.getWorkflowUser(userId));
    	actionTaken.setActionDate(Calendar.getInstance());
    	RouteHeaderDetailVO resultDetail = workflowInfo.routingSimulation(detail, new ActionTakenVO[] { actionTaken });
    	boolean lastApprover = true;
    	// see if there are any non-deactivated requests left at this level
    	for (int index = 0; index < resultDetail.getActionRequests().length; index++) {
			ActionRequestVO request = resultDetail.getActionRequests()[index];
			if (request.getRouteLevel().equals(routeLevel) && !KEWConstants.ACTION_REQUEST_DONE_STATE.equals(request.getStatus())) {
				lastApprover = false;
				break;
			}
		}
    	return lastApprover;
    	//return workflowInfo.isLastApproverInRouteLevel(routeHeaderId, userId, routeLevel);    
    }
    
    public boolean routeLevelHasApproverActionRequest(String docType, String docContent, Integer routeLevel) throws WorkflowException {
    	//DocumentContentVO documentContent = new DocumentContentVO();
    	//documentContent.setAttributeContent(docContent);
    	ReportCriteriaDTO criteria = new ReportCriteriaDTO(docType, docContent, routeLevel, routeLevel);
    	RouteHeaderDetailVO detail = workflowInfo.routingReport(criteria);
    	for (int index = 0; index < detail.getActionRequests().length; index++) {
			ActionRequestVO actionRequest = detail.getActionRequests()[index];
			if (actionRequest.isApprovalRequest()) {
				return true;
			}
		}
    	return false;
    }
    
    public boolean hasMember(WorkgroupVO workgroup, UserVO user) {
    	UserVO[] users = workgroup.getMembers();
    	for (int index = 0; index < users.length; index++) {
    		UserVO workgroupUser = users[index];
    		if (user.getNetworkId().equals(workgroupUser.getNetworkId())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean isUser(UserVO user, UserIdVO userId) {
    	boolean isUser = false;
    	if (userId instanceof EmplIdVO) {
    		isUser = ((EmplIdVO)userId).getEmplId().equals(user.getEmplId());
    	} else if (userId instanceof NetworkIdVO) {
    		isUser = ((NetworkIdVO)userId).getNetworkId().equals(user.getNetworkId());
    	} else if (userId instanceof UuIdVO) {
    		isUser = ((UuIdVO)userId).getUuId().equals(user.getUuId());
    	} else if (userId instanceof WorkflowIdVO) {
    		isUser = ((WorkflowIdVO)userId).getWorkflowId().equals(user.getWorkflowId());
    	}
    	return isUser;
    }*/
	
}
