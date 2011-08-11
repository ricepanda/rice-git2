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
package org.kuali.rice.kew.web.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.kew.actionlist.ActionListFilter;
import org.kuali.rice.kew.preferences.Preferences;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.user.UserUtils;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.PersonService;


/**
 * Represents an authenticated user within the Workflow system.
 *
 * <p>The current authenticated UserSession is stored in a ThreadLocal and can be
 * accessed using UserSession.getAuthenticatedUser().
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class UserSession implements Serializable {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(UserSession.class);

    private static final long serialVersionUID = 1L;

    private static ThreadLocal currentUserSession = new ThreadLocal();

    private int nextObjectKey;
    private transient Map objectMap = new HashMap();
    private ActionListFilter actionListFilter;
    private Preferences preferences;
    private List authentications = new ArrayList();

    private String sortOrder;
    private String sortCriteria;
    private int currentPage;
    private boolean updateActionList;

    private KimPrincipal actualPrincipal;
    private Person actualPerson;
    private Map<String, Group> actualPrincipalGroups = new HashMap<String, Group>();

    private KimPrincipal backdoorPrincipal;
    private Person backdoorPerson;
    private Map<String, Group> backdoorPrincipalGroups = new HashMap<String, Group>();

    private KimPrincipal helpDeskActionListPrincipal;
    private Person helpDeskActionListPerson;

    private static IdentityManagementService identityService = null;
    private static PersonService<Person> personService = null;

    public UserSession (KimPrincipal actualPrincipal) {
        init(actualPrincipal);
    }

	public UserSession (String actualPrincipalId) {
		init(getIdentityService().getPrincipal(actualPrincipalId));
    }

	private void init(KimPrincipal actualPrincipal) {
		if (actualPrincipal == null) {
			throw new IllegalArgumentException("UserSession was constructed with a null Principal");
		}
		this.actualPrincipal = actualPrincipal;
		actualPerson = getPersonService().getPerson(actualPrincipal.getPrincipalId());
		if ( actualPerson == null ) {
			throw new RuntimeException( "Unable to create person object from the given principal ID: " + actualPrincipal.getPrincipalId() );
		}
		establishPreferencesForPrincipal(actualPrincipal);
		List<? extends Group> groups = getIdentityService().getGroupsForPrincipal(actualPrincipal.getPrincipalId());
		for (Group group : groups) {
			actualPrincipalGroups.put(group.getGroupId(), group);
		}
        this.nextObjectKey = 0;
	}

	/**
	 * @return the sortOrder
	 */
	public String getSortOrder() {
		return this.sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the sortCriteria
	 */
	public String getSortCriteria() {
		return this.sortCriteria;
	}

	/**
	 * @param sortCriteria the sortCriteria to set
	 */
	public void setSortCriteria(String sortCriteria) {
		this.sortCriteria = sortCriteria;
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

    public static UserSession getAuthenticatedUser() {
    	return (UserSession)currentUserSession.get();
    }

    public static void setAuthenticatedUser(UserSession currentUserSession) {
    	UserSession.currentUserSession.set(currentUserSession);
    }

    public String getValue(String value) {
    	return value;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public ActionListFilter getActionListFilter() {
        return actionListFilter;
    }

    public void setActionListFilter(ActionListFilter actionListFilter) {
        this.actionListFilter = actionListFilter;
    }

    public KimPrincipal getActualPrincipal() {
    	return actualPrincipal;
    }

    public Person getActualPerson() {
    	return actualPerson;
    }

	public KimPrincipal getBackdoorPrincipal() {
		return this.backdoorPrincipal;
	}

	public Person getBackdoorPerson() {
		return this.backdoorPerson;
	}

	public KimPrincipal getHelpDeskActionListPrincipal() {
		return this.helpDeskActionListPrincipal;
	}

	public Person getHelpDeskActionListPerson() {
		return this.helpDeskActionListPerson;
	}

	public String getPrincipalId() {
    	return getPrincipal().getPrincipalId();
    }

    public String getPrincipalName() {
    	return getPrincipal().getPrincipalName();
    }

    public KimPrincipal getPrincipal() {
    	if (getBackdoorPrincipal() != null) {
    		return getBackdoorPrincipal();
    	} else if (getActualPrincipal() != null) {
    		return getActualPrincipal();
    	}
    	throw new IllegalStateException("UserSession does not contain an established principal.");
    }

    public Person getPerson() {
    	if (getBackdoorPerson() != null) {
    		return getBackdoorPerson();
    	} else if (getActualPerson() != null) {
    		return getActualPerson();
    	}
    	throw new IllegalStateException("UserSession does not contain an established person.");
    }

    public boolean establishBackdoorWithPrincipalName(String principalName) {
        if (!isProductionEnvironment()) {
        	this.backdoorPrincipal = KIMServiceLocator.getIdentityManagementService().getPrincipalByPrincipalName(principalName);
        	if (backdoorPrincipal == null) {
        		return false;
        	}
        	this.backdoorPerson = KEWServiceLocator.getIdentityHelperService().getPersonByPrincipalName(principalName);
        	List<? extends Group> groups = KIMServiceLocator.getIdentityManagementService().getGroupsForPrincipal(backdoorPrincipal.getPrincipalId());
    		for (Group group : groups) {
    			backdoorPrincipalGroups.put(group.getGroupId(), group);
    		}
        	establishPreferencesForPrincipal(backdoorPrincipal);
        	return true;
        }
        return false;
    }

    public void clearBackdoor() {
        this.backdoorPrincipal = null;
        this.backdoorPerson = null;
        establishPreferencesForPrincipal(actualPrincipal);
    }

    public void establishHelpDeskWithPrincipalName(String principalName) {
    	this.helpDeskActionListPrincipal = KEWServiceLocator.getIdentityHelperService().getPrincipalByPrincipalName(principalName);
    	this.helpDeskActionListPerson = KEWServiceLocator.getIdentityHelperService().getPersonByPrincipalName(principalName);
    }

    public void clearHelpDesk() {
    	this.helpDeskActionListPrincipal = null;
    	this.helpDeskActionListPerson = null;
    }

    public void refreshPreferences() {
    	establishPreferencesForPrincipal(getPrincipal());
    	this.updateActionList = true;
    	//this.preferences.setPreferencesUpdated(true);
    }

    protected void establishPreferencesForPrincipal(KimPrincipal principal) {
        this.preferences = KEWServiceLocator.getPreferencesService().getPreferences(principal.getPrincipalId());
        if (this.preferences.isRequiresSave()) {
            LOG.info("Detected that user preferences require saving.");
            try {
            	KEWServiceLocator.getPreferencesService().savePreferences(principal.getPrincipalId(), this.preferences);
            } catch (Exception e) {
            	LOG.warn("Failed to save preferences for user!  Likely user tried to log in from more than one browser at the same time.  Reloading preferences.");
            }
            this.preferences = KEWServiceLocator.getPreferencesService().getPreferences(principal.getPrincipalId());
        }
    }

    protected boolean isProductionEnvironment() {
    	return ConfigContext.getCurrentContextConfig().getProperty(KEWConstants.PROD_DEPLOYMENT_CODE).equalsIgnoreCase(
    			ConfigContext.getCurrentContextConfig().getEnvironment());
    }

    public String addObject(Object object) {
        String objectKey = nextObjectKey++ + "";
        getObjectMap().put(objectKey, object);
        return objectKey;
    }

    public Object retrieveObject(String objectKey) {
        return getObjectMap().get(objectKey);
    }

    public void removeObject(String objectKey) {
        getObjectMap().remove(objectKey);
    }

    public boolean isBackdoorInUse() {
        return backdoorPrincipal != null;
    }

    public String getEmailAddress() {
    	return getPerson().getEmailAddressUnmasked();
    }

    public int getNextObjectKey() {
        return nextObjectKey;
    }
    public void setNextObjectKey(int nextObjectKey) {
        this.nextObjectKey = nextObjectKey;
    }
    public Map getObjectMap() {
    	if (objectMap == null) {
    		objectMap = new HashMap();
    	}
        return objectMap;
    }
    public void setObjectMap(Map objectMap) {
        this.objectMap = objectMap;
    }
    public String getDisplayName() {
        return getPersonService().getPerson(getPrincipalId()).getNameUnmasked();
    }

    /**
     * Returns a List of Authentications on the UserSession.  This List identifies the various types of
     * authentications that the user has performed (i.e. Kerberos, Safeword, etc.)
     */
    public List getAuthentications() {
    	return authentications;
    }

    public void addAuthentication(Authentication authentication) {
    	getAuthentications().add(authentication);
    }

    public void removeAuthentication(Authentication authentication) {
    	getAuthentications().remove(authentication);
    } 

    public boolean hasRole(String role) {
    	for (Iterator iterator = getAuthentications().iterator(); iterator.hasNext();) {
    		Authentication auth = (Authentication) iterator.next();
    		if (auth.getAuthority().equalsIgnoreCase(role)) {
    			return true;
    		}
    	}
    	return false;
    }

    public Map<String, Group> getGroups() {
    	if (getBackdoorPrincipal() != null) {
    		return backdoorPrincipalGroups;
    	}
		return actualPrincipalGroups;
	}

	public boolean isMemberOfGroupWithName(String namespace, String groupName) {
		for (Group group : getGroups().values()) {
			if (StringUtils.equals(namespace, group.getNamespaceCode()) && StringUtils.equals(groupName, group.getGroupName())) {
				return true;
			}
		}
		return false;
	}

	public boolean isMemberOfGroupWithId(String groupId) {
		return getGroups().containsKey(groupId);
	}

	protected IdentityManagementService getIdentityService(){
		if(identityService == null){
			identityService = KIMServiceLocator.getIdentityManagementService();

		}
		return identityService;
	}

	protected PersonService<Person> getPersonService(){
		if(personService == null){
			personService = KIMServiceLocator.getPersonService();
		}
		return personService;
	}

    public boolean isUpdateActionList() {
        return this.updateActionList;
    }

    public void actionListUpdated() {
        this.updateActionList = false;
    }

}
