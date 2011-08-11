/*
 * Copyright 2005-2009 The Kuali Foundation
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
package org.kuali.rice.kew.actionlist.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.rice.core.util.JSTLConstants;
import org.kuali.rice.kew.actionlist.ActionListFilter;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.preferences.Preferences;
import org.kuali.rice.kew.preferences.service.PreferencesService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.WebFriendlyRecipient;
import org.kuali.rice.kew.web.KeyValue;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.web.struts.action.KualiAction;


/**
 * Action for Action List Filter page.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ActionListFilterAction extends KualiAction {

    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
    	request.setAttribute("Constants", new JSTLConstants(KEWConstants.class));
    	request.setAttribute("preferences", this.getUserSession(request).getPreferences());
		initForm(request, form);
		return super.execute(mapping, form, request, response);
	}

	/**
	 * This overridden method ...
	 *
	 * @see org.kuali.rice.kns.web.struts.action.KualiAction#getReturnLocation(javax.servlet.http.HttpServletRequest, org.apache.struts.action.ActionMapping)
	 */
	@Override
	protected String getReturnLocation(HttpServletRequest request,
			ActionMapping mapping)
	{
    	String mappingPath = mapping.getPath();
    	String basePath = getBasePath(request);
        return basePath + KEWConstants.WEBAPP_DIRECTORY + mappingPath + ".do";
	}

	public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionListFilterForm filterForm = (ActionListFilterForm) form;
        if (getUserSession(request).getActionListFilter() != null) {
            ActionListFilter actionListFilter = getUserSession(request).getActionListFilter();
            if (filterForm.getDocTypeFullName() != null && ! "".equals(filterForm.getDocTypeFullName())) {
                actionListFilter.setDocumentType(filterForm.getDocTypeFullName());
                getUserSession(request).setActionListFilter(actionListFilter);
                filterForm.setFilter(actionListFilter);
            } else {
                filterForm.setFilter(actionListFilter);
                filterForm.setDocTypeFullName(actionListFilter.getDocumentType());
            }
        }
        return mapping.findForward("viewFilter");
    }

    public ActionForward filter(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionListFilterForm filterForm = (ActionListFilterForm) form;
        //validate the filter through the actionitem/actionlist service (I'm thinking actionlistservice)
        UserSession session = getUserSession(request);
        ActionListFilter alFilter = filterForm.getLoadedFilter();
        if (StringUtils.isNotBlank(alFilter.getDelegatorId()) && !KEWConstants.DELEGATION_DEFAULT.equals(alFilter.getDelegatorId()) &&
        		StringUtils.isNotBlank(alFilter.getPrimaryDelegateId()) && !KEWConstants.PRIMARY_DELEGATION_DEFAULT.equals(alFilter.getPrimaryDelegateId())){
        	// If the primary and secondary delegation drop-downs are both visible and are both set to non-default values,
        	// then reset the secondary delegation drop-down to its default value.
        	alFilter.setDelegatorId(KEWConstants.DELEGATION_DEFAULT);
        }
        session.setActionListFilter(alFilter);
        KEWServiceLocator.getActionListService().saveRefreshUserOption(getUserSession(request).getPrincipal().getPrincipalId());
        if (GlobalVariables.getMessageMap().isEmpty()) {
            return mapping.findForward("viewActionList");
        } else {
            return mapping.findForward("viewFilter");
        }
    }

    public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionListFilterForm filterForm = (ActionListFilterForm) form;
        filterForm.setFilter(new ActionListFilter());
        filterForm.setCreateDateFrom("");
        filterForm.setCreateDateTo("");
        filterForm.setLastAssignedDateFrom("");
        filterForm.setLastAssignedDateTo("");
        filterForm.setDocTypeFullName("");
        UserSession session = getUserSession(request);
        session.setActionListFilter(null);
        KEWServiceLocator.getActionListService().saveRefreshUserOption(getUserSession(request).getPrincipal().getPrincipalId());
        return mapping.findForward("viewFilter");
    }

    public void initForm(HttpServletRequest request, ActionForm form) throws Exception {
        ActionListFilterForm filterForm = (ActionListFilterForm) form;
        filterForm.setUserWorkgroups(getUserWorkgroupsDropDownList(getUserSession(request).getPerson().getPrincipalId()));
        PreferencesService prefSrv = (PreferencesService) KEWServiceLocator.getPreferencesService();
        Preferences preferences = prefSrv.getPreferences(getUserSession(request).getPerson().getPrincipalId());
        request.setAttribute("preferences", preferences);
        ActionListService actionListSrv = (ActionListService) KEWServiceLocator.getActionListService();
        request.setAttribute("delegators", getWebFriendlyRecipients(actionListSrv.findUserSecondaryDelegators(getUserSession(request).getPrincipal().getPrincipalId())));
        request.setAttribute("primaryDelegates", getWebFriendlyRecipients(actionListSrv.findUserPrimaryDelegations(getUserSession(request).getPrincipal().getPrincipalId())));
        if (! filterForm.getMethodToCall().equalsIgnoreCase("clear")) {
            filterForm.validateDates();
        }
    }

    private List getUserWorkgroupsDropDownList(String principalId) {
    	List<String> userWorkgroups =
            KIMServiceLocator.getIdentityManagementService().getGroupIdsForPrincipal(principalId);
        List<KeyValue> sortedUserWorkgroups = new ArrayList();
    	KeyValue keyValue = null;
    	keyValue = new KeyValue(KEWConstants.NO_FILTERING, KEWConstants.NO_FILTERING);
    	sortedUserWorkgroups.add(keyValue);
    	if (userWorkgroups != null && userWorkgroups.size() > 0) {
    		Collections.sort(userWorkgroups);

    		Group group;
            for (String groupId : userWorkgroups)
            {
                group = KIMServiceLocator.getIdentityManagementService().getGroup(groupId);
                keyValue = new KeyValue(groupId, group.getGroupName());
                sortedUserWorkgroups.add(keyValue);
            }
    	}
    	return sortedUserWorkgroups;
    }

    private List getWebFriendlyRecipients(Collection recipients) {
        Collection newRecipients = new ArrayList(recipients.size());
        for (Iterator iterator = recipients.iterator(); iterator.hasNext();) {
            newRecipients.add(new WebFriendlyRecipient( iterator.next()));
        }
        List recipientList = new ArrayList(newRecipients);
        Collections.sort(recipientList, new Comparator() {
            Comparator comp = new ComparableComparator();
            public int compare(Object o1, Object o2) {
                return comp.compare(((WebFriendlyRecipient)o1).getDisplayName().trim().toLowerCase(), ((WebFriendlyRecipient)o2).getDisplayName().trim().toLowerCase());
            }
        });
        return recipientList;
    }

	private UserSession getUserSession(HttpServletRequest request){
		return UserSession.getAuthenticatedUser();
	}

}

