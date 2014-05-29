/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.web.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifConstants.ViewType;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.lifecycle.ViewPostMetadata;
import org.kuali.rice.krad.uif.service.ViewHelperService;
import org.kuali.rice.krad.uif.service.ViewService;
import org.kuali.rice.krad.uif.util.SessionTransient;
import org.kuali.rice.krad.uif.view.DialogManager;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.bind.RequestAccessible;
import org.springframework.web.multipart.MultipartFile;

/**
 * Base form class for views within the KRAD User Interface Framework.
 *
 * <p>Holds properties necessary to determine the {@link org.kuali.rice.krad.uif.view.View} instance that
 * will be used to render the user interface</p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class UifFormBase implements ViewModel {
    private static final long serialVersionUID = 8432543267099454434L;

    @RequestAccessible
    protected String viewId;

    @RequestAccessible
    protected String viewName;

    @RequestAccessible
    protected ViewType viewTypeName;

    @RequestAccessible
    protected String pageId;

    @RequestAccessible
    protected String methodToCall;

    @RequestAccessible
    protected String formKey;

    @RequestAccessible
    @SessionTransient
    protected String requestedFormKey;

    @RequestAccessible
    protected String flowKey;

    protected String sessionId;
    protected int sessionTimeoutInterval;

    @SessionTransient
    protected HistoryFlow historyFlow;
    @SessionTransient
    protected HistoryManager historyManager;

    @RequestAccessible
    @SessionTransient
    protected String jumpToId;

    @SessionTransient
    protected String jumpToName;

    @RequestAccessible
    @SessionTransient
    protected String focusId;

    @RequestAccessible
    @SessionTransient
    protected boolean dirtyForm;

    protected String formPostUrl;
    protected String controllerMapping;

    @SessionTransient
    private String requestUrl;
    private Map<String, String[]> initialRequestParameters;

    protected String state;

    @RequestAccessible
    protected boolean renderedInLightBox;

    @RequestAccessible
    protected boolean renderedInIframe;

    @SessionTransient
    protected String growlScript;

    @SessionTransient
    protected String lightboxScript;

    @SessionTransient
    protected View view;
    protected ViewPostMetadata viewPostMetadata;

    protected Map<String, String> viewRequestParameters;
    protected List<String> readOnlyFieldsList;

    protected Map<String, Object> newCollectionLines;

    @RequestAccessible
    @SessionTransient
    protected Map<String, String> actionParameters;

    protected Map<String, Object> clientStateForSyncing;

    @SessionTransient
    protected Map<String, Set<String>> selectedCollectionLines;

    protected Set<String> selectedLookupResultsCache;

    protected List<Object> addedCollectionItems;

    @SessionTransient
    protected MultipartFile attachmentFile;

    // navigation
    @RequestAccessible
    protected String returnLocation;

    @RequestAccessible
    protected String returnFormKey;

    @RequestAccessible
    @SessionTransient
    protected boolean ajaxRequest;

    @RequestAccessible
    @SessionTransient
    protected String ajaxReturnType;

    @SessionTransient
    private String requestJsonTemplate;
    @SessionTransient
    private boolean collectionPagingRequest;

    // dialog fields
    @RequestAccessible
    @SessionTransient
    protected String dialogExplanation;

    @RequestAccessible
    @SessionTransient
    protected String dialogResponse;
    protected DialogManager dialogManager;

    @SessionTransient
    protected boolean requestRedirected;

    @RequestAccessible
    @SessionTransient
    protected String updateComponentId;
    @SessionTransient
    private Component updateComponent;

    @RequestAccessible
    protected Map<String, Object> extensionData;

    protected Map<String, String> queryParameters;
    protected boolean applyDefaultValues;

    public UifFormBase() {
        renderedInLightBox = false;
        renderedInIframe = false;
        requestRedirected = false;

        readOnlyFieldsList = new ArrayList<String>();
        viewRequestParameters = new HashMap<String, String>();
        newCollectionLines = new HashMap<String, Object>();
        actionParameters = new HashMap<String, String>();
        clientStateForSyncing = new HashMap<String, Object>();
        selectedCollectionLines = new HashMap<String, Set<String>>();
        selectedLookupResultsCache = new HashSet<String>();
        addedCollectionItems = new ArrayList<Object>();
        dialogManager = new DialogManager();
        extensionData = new HashMap<String, Object>();
        queryParameters = new HashMap<String, String>();
        applyDefaultValues = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preBind(HttpServletRequest request) {
        // do nothing - here for framework
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#postBind(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public void postBind(HttpServletRequest request) {
        // assign form key if this is a new form or the requested form key is not in session
        UifFormManager uifFormManager = (UifFormManager) request.getSession().getAttribute(UifParameters.FORM_MANAGER);
        if (StringUtils.isBlank(formKey) || !uifFormManager.hasSessionForm(formKey)) {
            formKey = generateFormKey();
        }

        // default form post URL to request URL
        formPostUrl = request.getRequestURL().toString();

        if (request.getSession() != null) {
            sessionId = request.getSession().getId();
            sessionTimeoutInterval = request.getSession().getMaxInactiveInterval();
        }

        //set controller mapping property
        controllerMapping = request.getPathInfo();

        // get any sent client view state and parse into map
        if (request.getParameterMap().containsKey(UifParameters.CLIENT_VIEW_STATE)) {
                    String clientStateJSON = request.getParameter(UifParameters.CLIENT_VIEW_STATE);
                    if (StringUtils.isNotBlank(clientStateJSON)) {
                        // change single quotes to double quotes (necessary because the reverse was done for sending)
                        clientStateJSON = StringUtils.replace(clientStateJSON, "'", "\"");

                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            clientStateForSyncing = mapper.readValue(clientStateJSON, Map.class);
                        } catch (IOException e) {
                            throw new RuntimeException("Unable to decode client side state JSON", e);
                        }
                    }
                }

        // populate read only fields list
        if (request.getParameter(UifParameters.READ_ONLY_FIELDS) != null) {
            String readOnlyFields = request.getParameter(UifParameters.READ_ONLY_FIELDS);
            setReadOnlyFieldsList(KRADUtils.convertStringParameterToList(readOnlyFields));
        }

        // clean parameters from XSS attacks that will be written out as hiddens
        this.pageId = KRADUtils.stripXSSPatterns(this.pageId);
        this.methodToCall = KRADUtils.stripXSSPatterns(this.methodToCall);
        this.formKey = KRADUtils.stripXSSPatterns(this.formKey);
        this.requestedFormKey = KRADUtils.stripXSSPatterns(this.requestedFormKey);
        this.flowKey = KRADUtils.stripXSSPatterns(this.flowKey);
        this.sessionId = KRADUtils.stripXSSPatterns(this.sessionId);
        this.formPostUrl = KRADUtils.stripXSSPatterns(this.formPostUrl);
        this.returnLocation = KRADUtils.stripXSSPatterns(this.returnLocation);
        this.returnFormKey = KRADUtils.stripXSSPatterns(this.returnFormKey);
        this.requestUrl = KRADUtils.stripXSSPatterns(this.requestUrl);
    }

    /**
     * Creates the unique id used to store this "conversation" in the session.
     * The default method generates a java UUID.
     *
     * @return UUID
     */
    protected String generateFormKey() {
        return UUID.randomUUID().toString();
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getViewId()
     */
    @Override
    public String getViewId() {
        return this.viewId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setViewId(String)
     */
    @Override
    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getViewName()
     */
    @Override
    public String getViewName() {
        return this.viewName;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setViewName(String)
     */
    @Override
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getViewTypeName()
     */
    @Override
    public ViewType getViewTypeName() {
        return this.viewTypeName;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setViewTypeName(org.kuali.rice.krad.uif.UifConstants.ViewType)
     */
    @Override
    public void setViewTypeName(ViewType viewTypeName) {
        this.viewTypeName = viewTypeName;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getPageId()
     */
    @Override
    public String getPageId() {
        return this.pageId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setPageId(String)
     */
    @Override
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getFormPostUrl()
     */
    @Override
    public String getFormPostUrl() {
        return this.formPostUrl;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setFormPostUrl(String)
     */
    @Override
    public void setFormPostUrl(String formPostUrl) {
        this.formPostUrl = formPostUrl;
    }

    /**
     * Name of the controllerMapping for this form (includes slash)
     *
     * @return the controllerMapping string
     */
    public String getControllerMapping() {
        return controllerMapping;
    }

    /**
     * The current {@link HistoryFlow} for this form which stores a trail of urls/breadcrumbs primarily used for
     * path-based breadcrumb display
     *
     * @return the {@link HistoryFlow}
     */
    public HistoryFlow getHistoryFlow() {
        return historyFlow;
    }

    /**
     * Set the current HistoryFlow for this form
     *
     * @param historyFlow
     */
    public void setHistoryFlow(HistoryFlow historyFlow) {
        this.historyFlow = historyFlow;
    }

    /**
     * The current {@link HistoryManager} that was pulled from session which store all {@link HistoryFlow} objects in
     * the current session to keep track of the path the user has taken across views (primarily used by path-based
     * breadcrumbs)
     *
     * @return the HistoryManager
     */
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    /**
     * Set the current HistoryManager
     *
     * @param historyManager
     */
    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    /**
     * The flowKey representing the HistoryFlow this form may be in.
     *
     * <p>This allows for a flow to continue by key or start (if set to "start").
     * If null or blank, no flow (or path based
     * breadcrumbs) are being tracked.</p>
     *
     * @return the flowKey
     */
    public String getFlowKey() {
        return flowKey;
    }

    /**
     * Set the flowKey
     *
     * @param flowKey
     */
    public void setFlowKey(String flowKey) {
        this.flowKey = flowKey;
    }

    /**
     * The original requestUrl for the View represented by this form (url received by the controller for initial
     * request)
     *
     * @return the requestUrl
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Set the requestUrl
     *
     * @param requestUrl
     */
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * The requestParameters represent all the parameters in the query string that were initially passed to this View
     * by the initial request
     *
     * @return the requestParameters
     */
    public Map<String, String[]> getInitialRequestParameters() {
        return initialRequestParameters;
    }

    /**
     * Set the requestParameters
     *
     * @param requestParameters
     */
    public void setInitialRequestParameters(Map<String, String[]> requestParameters) {
        this.initialRequestParameters = requestParameters;
    }

    public String getReturnLocation() {
        return this.returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }

    public String getReturnFormKey() {
        return this.returnFormKey;
    }

    public void setReturnFormKey(String returnFormKey) {
        this.returnFormKey = returnFormKey;
    }

    /**
     * Holds the id for the user's current session
     *
     * <p>
     * The user's session id is used to track when a timeout has occurred and enforce the policy
     * configured with the {@link org.kuali.rice.krad.uif.view.ViewSessionPolicy}. This property gets initialized
     * in the {@link #postBind(javax.servlet.http.HttpServletRequest)} method and then is written out as a
     * hidden on the view. Therefore each post done on the view will send back the session id when the view was
     * rendering, and the {@link org.kuali.rice.krad.web.filter.UifSessionTimeoutFilter} can use that to determine
     * if a timeout has occurred
     * </p>
     *
     * @return id for the user's current session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Holds the configured session timeout interval
     *
     * <p>
     * Holds the session timeout interval so it can be referenced to give the user notifications (for example the
     * session timeout warning reads this property). This is initialized from the session object in
     * {@link #postBind(javax.servlet.http.HttpServletRequest)}
     * </p>
     *
     * @return amount of time in milliseconds before the session will timeout
     */
    public int getSessionTimeoutInterval() {
        return sessionTimeoutInterval;
    }

    /**
     * Identifies the controller method that should be invoked to fulfill a
     * request. The value will be matched up against the 'params' setting on the
     * {@code RequestMapping} annotation for the controller method
     *
     * @return String method to call
     */
    public String getMethodToCall() {
        return this.methodToCall;
    }

    /**
     * Setter for the method to call
     *
     * @param methodToCall
     */
    public void setMethodToCall(String methodToCall) {
        this.methodToCall = methodToCall;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getViewRequestParameters() {
        return this.viewRequestParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewRequestParameters(Map<String, String> viewRequestParameters) {
        this.viewRequestParameters = viewRequestParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getReadOnlyFieldsList() {
        return readOnlyFieldsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReadOnlyFieldsList(List<String> readOnlyFieldsList) {
        this.readOnlyFieldsList = readOnlyFieldsList;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getNewCollectionLines()
     */
    @Override
    public Map<String, Object> getNewCollectionLines() {
        return this.newCollectionLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNewCollectionLines(Map<String, Object> newCollectionLines) {
        this.newCollectionLines = newCollectionLines;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getActionParameters()
     */
    @Override
    public Map<String, String> getActionParameters() {
        return this.actionParameters;
    }

    /**
     * Returns the action parameters map as a {@code Properties} instance
     *
     * @return Properties action parameters
     */
    public Properties getActionParametersAsProperties() {
        return KRADUtils.convertMapToProperties(actionParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActionParameters(Map<String, String> actionParameters) {
        this.actionParameters = actionParameters;
    }

    /**
     * Retrieves the value for the given action parameter, or empty string if
     * not found
     *
     * @param actionParameterName - name of the action parameter to retrieve value for
     * @return String parameter value or empty string
     */
    public String getActionParamaterValue(String actionParameterName) {
        if ((actionParameters != null) && actionParameters.containsKey(actionParameterName)) {
            return actionParameters.get(actionParameterName);
        }

        return "";
    }

    /**
     * Returns the action event that was sent in the action parameters (if any)
     *
     * <p>
     * The action event is a special action parameter that can be sent to indicate a type of action being taken. This
     * can be looked at by the view or components to render differently
     * </p>
     *
     * TODO: make sure action parameters are getting reinitialized on each request
     *
     * @return String action event name or blank if action event was not sent
     */
    public String getActionEvent() {
        if ((actionParameters != null) && actionParameters.containsKey(UifConstants.UrlParams.ACTION_EVENT)) {
            return actionParameters.get(UifConstants.UrlParams.ACTION_EVENT);
        }

        return "";
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getClientStateForSyncing()
     */
    @Override
    public Map<String, Object> getClientStateForSyncing() {
        return clientStateForSyncing;
    }

    /**
     * Setter for the client state
     *
     * @param clientStateForSyncing
     */
    public void setClientStateForSyncing(Map<String, Object> clientStateForSyncing) {
        this.clientStateForSyncing = clientStateForSyncing;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getSelectedCollectionLines()
     */
    @Override
    public Map<String, Set<String>> getSelectedCollectionLines() {
        return selectedCollectionLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedCollectionLines(Map<String, Set<String>> selectedCollectionLines) {
        this.selectedCollectionLines = selectedCollectionLines;
    }

    /**
     * Holds Set of String identifiers for lines that were selected in a lookup collection results
     * across multiple pages.
     * The value in the cache is preserved in the session across multiple requests. This allows for the
     * server side paging of results to retain the user choices as they move through the pages.
     * 
     * @return set of identifiers
     */
    public Set<String> getSelectedLookupResultsCache() {
        return selectedLookupResultsCache;
    }

    /**
     * Sets the lookup result selection cache values
     *
     * @param selectedLookupResultsCache
     */
    public void setSelectedLookupResultsCache(Set<String> selectedLookupResultsCache) {
        this.selectedLookupResultsCache = selectedLookupResultsCache;
    }

    /**
     * Key string that identifies the form instance in session storage
     *
     * <p>
     * When the view is posted, the previous form instance is retrieved and then
     * populated from the request parameters. This key string is retrieve the
     * session form from the session service
     * </p>
     *
     * @return String form session key
     */
    public String getFormKey() {
        return this.formKey;
    }

    /**
     * Setter for the form's session key
     *
     * @param formKey
     */
    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    /**
     * This is the formKey sent on the original request.  It may differ from the actual form key stored in formKey
     * based on if the form still exists in session by this key or not.
     *
     * @return the original requested form key
     */
    public String getRequestedFormKey() {
        return requestedFormKey;
    }

    /**
     * Set the requestedFormKey
     *
     * @param requestedFormKey
     */
    public void setRequestedFormKey(String requestedFormKey) {
        this.requestedFormKey = requestedFormKey;
    }

    /**
     * Indicates whether a redirect has been requested for the view
     *
     * @return boolean true if redirect was requested, false if not
     */
    public boolean isRequestRedirected() {
        return requestRedirected;
    }

    /**
     * Setter for the request redirect indicator
     *
     * @param requestRedirected
     */
    public void setRequestRedirected(boolean requestRedirected) {
        this.requestRedirected = requestRedirected;
    }
    /**
     * Holder for files that are attached through the view
     *
     * @return MultipartFile representing the attachment
     */
    public MultipartFile getAttachmentFile() {
        return this.attachmentFile;
    }

    /**
     * Setter for the form's attachment file
     *
     * @param attachmentFile
     */
    public void setAttachmentFile(MultipartFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getUpdateComponentId()
     */
    @Override
    public String getUpdateComponentId() {
        return updateComponentId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setUpdateComponentId(java.lang.String)
     */
    @Override
    public void setUpdateComponentId(String updateComponentId) {
        this.updateComponentId = updateComponentId;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getUpdateComponent()
     */
    public Component getUpdateComponent() {
        return updateComponent;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setUpdateComponent(org.kuali.rice.krad.uif.component.Component)
     */
    public void setUpdateComponent(Component updateComponent) {
        this.updateComponent = updateComponent;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getView()
     */
    @Override
    public View getView() {
        return this.view;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setView(org.kuali.rice.krad.uif.view.View)
     */
    @Override
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Returns an instance of the view's configured view helper service.
     *
     * <p>First checks if there is an initialized view containing a view helper instance. If not, and there is
     * a view id on the form, a call is made to retrieve the view helper instance or class configuration.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public ViewHelperService getViewHelperService() {
        if ((getView() != null) && (getView().getViewHelperService() != null)) {
            return getView().getViewHelperService();
        }

        String viewId = getViewId();
        if (StringUtils.isBlank(viewId) && (getView() != null)) {
            viewId = getView().getId();
        }

        if (StringUtils.isBlank(viewId)) {
            return null;
        }

        ViewHelperService viewHelperService =
                (ViewHelperService) KRADServiceLocatorWeb.getDataDictionaryService().getDictionaryBeanProperty(viewId,
                        UifPropertyPaths.VIEW_HELPER_SERVICE);
        if (viewHelperService == null) {
            Class<?> viewHelperServiceClass =
                    (Class<?>) KRADServiceLocatorWeb.getDataDictionaryService().getDictionaryBeanProperty(viewId,
                            UifPropertyPaths.VIEW_HELPER_SERVICE_CLASS);

            if (viewHelperServiceClass != null) {
                try {
                    viewHelperService = (ViewHelperService) viewHelperServiceClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Unable to instantiate view helper class: " + viewHelperServiceClass, e);
                }
            }
        }

        return viewHelperService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewPostMetadata getViewPostMetadata() {
        return viewPostMetadata;
    }

    /**
     * @see UifFormBase#getViewPostMetadata()
     */
    @Override
    public void setViewPostMetadata(ViewPostMetadata viewPostMetadata) {
        this.viewPostMetadata = viewPostMetadata;
    }

    /**
     * Instance of the {@code ViewService} that can be used to retrieve
     * {@code View} instances
     *
     * @return ViewService implementation
     */
    protected ViewService getViewService() {
        return KRADServiceLocatorWeb.getViewService();
    }

    /**
     * The jumpToId for this form, the element with this id will be jumped to automatically
     * when the form is loaded in the view.
     * Using "TOP" or "BOTTOM" will jump to the top or the bottom of the resulting page.
     * jumpToId always takes precedence over jumpToName, if set.
     *
     * @return the jumpToId
     */
    public String getJumpToId() {
        return this.jumpToId;
    }

    /**
     * @param jumpToId the jumpToId to set
     */
    public void setJumpToId(String jumpToId) {
        this.jumpToId = jumpToId;
    }

    /**
     * The jumpToName for this form, the element with this name will be jumped to automatically
     * when the form is loaded in the view.
     * WARNING: jumpToId always takes precedence over jumpToName, if set.
     *
     * @return the jumpToName
     */
    public String getJumpToName() {
        return this.jumpToName;
    }

    /**
     * @param jumpToName the jumpToName to set
     */
    public void setJumpToName(String jumpToName) {
        this.jumpToName = jumpToName;
    }

    /**
     * Field to place focus on when the page loads
     * An empty focusId will result in focusing on the first visible input element by default.
     *
     * @return the focusId
     */
    public String getFocusId() {
        return this.focusId;
    }

    /**
     * @param focusId the focusId to set
     */
    public void setFocusId(String focusId) {
        this.focusId = focusId;
    }

    /**
     * True when the form is considered dirty (data has changed from original value), false otherwise
     *
     * <p>For most scenarios, this flag should NOT be set to true.
     * If this is set, it must be managed explicitly by the application.  This flag exists for marking a
     * form dirty from a server call, so it must be changed to false when the form is no longer considered dirty.
     * The krad save Action and navigate methodToCall resets this flag back to false, but any other setting of
     * this flag must be managed by custom configuration/methods, if custom dirtyForm management is needed.</p>
     *
     * @return true if the form is considered dirty, false otherwise
     */
    public boolean isDirtyForm() {
        return dirtyForm;
    }

    /**
     * Sets the dirtyForm flag
     *
     * <p>For most scenarios, this flag should NOT be set to true.
     * If this is set, it must be managed explicitly by the application.  This flag exists for marking a
     * form dirty from a server call, so it must be changed to false when the form is no longer considered dirty.
     * The krad save Action and navigate methodToCall resets this flag back to false, but any other setting of
     * this flag must be managed by custom configuration/methods, if custom dirtyForm management is needed.</p>
     *
     * @param dirtyForm
     */
    public void setDirtyForm(boolean dirtyForm) {
        this.dirtyForm = dirtyForm;
    }

    /**
     * Set the dirtyForm flag using a String that will be converted to boolean
     *
     * @param dirtyForm
     */
    public void setDirtyForm(String dirtyForm) {
        if(dirtyForm != null){
            this.dirtyForm = Boolean.parseBoolean(dirtyForm);
        }
    }

    /**
     * Indicates whether the view is rendered within a lightbox
     *
     * <p>
     * Some discussion (for example how a close button behaves) need to change based on whether the
     * view is rendered within a lightbox or the standard browser window. This boolean is true when it is
     * within a lightbox
     * </p>
     *
     * @return boolean true if view is rendered within a lightbox, false if not
     */
    public boolean isRenderedInLightBox() {
        return this.renderedInLightBox;
    }

    /**
     * Setter for the rendered within lightbox indicator
     *
     * @param renderedInLightBox
     */
    public void setRenderedInLightBox(boolean renderedInLightBox) {
        this.renderedInLightBox = renderedInLightBox;
    }

    /**
     * Indicates whether the view is rendered within an iframe (this setting must be passed to the View on the url)
     *
     * @return boolean true if view is rendered within a iframe, false if not
     */
    public boolean isRenderedInIframe() {
        return renderedInIframe;
    }

    /**
     * @see org.kuali.rice.krad.web.form.UifFormBase#isRenderedInIframe()
     */
    public void setRenderedInIframe(boolean renderedInIframe) {
        this.renderedInIframe = renderedInIframe;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isApplyDefaultValues()
     */
    @Override
    public boolean isApplyDefaultValues() {
        return applyDefaultValues;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setApplyDefaultValues(boolean)
     */
    @Override
    public void setApplyDefaultValues(boolean applyDefaultValues) {
        this.applyDefaultValues = applyDefaultValues;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getGrowlScript()
     */
    @Override
    public String getGrowlScript() {
        return growlScript;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setGrowlScript(String)
     */
    @Override
    public void setGrowlScript(String growlScript) {
        this.growlScript = growlScript;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getState()
     */
    @Override
    public String getState() {
        return state;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setState(String)
     */
    @Override
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getLightboxScript()
     */
    @Override
    public String getLightboxScript() {
        return lightboxScript;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setLightboxScript(String)
     */
    @Override
    public void setLightboxScript(String lightboxScript) {
        this.lightboxScript = lightboxScript;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isAjaxRequest()
     */
    @Override
    public boolean isAjaxRequest() {
        return ajaxRequest;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setAjaxRequest(boolean)
     */
    @Override
    public void setAjaxRequest(boolean ajaxRequest) {
        this.ajaxRequest = ajaxRequest;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getAjaxReturnType()
     */
    @Override
    public String getAjaxReturnType() {
        return ajaxReturnType;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setAjaxReturnType(String)
     */
    @Override
    public void setAjaxReturnType(String ajaxReturnType) {
        this.ajaxReturnType = ajaxReturnType;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isUpdateComponentRequest()
     */
    @Override
    public boolean isUpdateComponentRequest() {
        return isAjaxRequest() && StringUtils.isNotBlank(getAjaxReturnType()) && getAjaxReturnType().equals(
                UifConstants.AjaxReturnTypes.UPDATECOMPONENT.getKey());
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isUpdateDialogRequest()
     */
    @Override
    public boolean isUpdateDialogRequest() {
        return isAjaxRequest() && StringUtils.isNotBlank(getAjaxReturnType()) && getAjaxReturnType().equals(
                UifConstants.AjaxReturnTypes.UPDATEDIALOG.getKey());
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isUpdatePageRequest()
     */
    @Override
    public boolean isUpdatePageRequest() {
        return StringUtils.isNotBlank(getAjaxReturnType()) && getAjaxReturnType().equals(
                UifConstants.AjaxReturnTypes.UPDATEPAGE.getKey());
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isUpdateNoneRequest()
     */
    @Override
    public boolean isUpdateNoneRequest() {
        //return isAjaxRequest() && StringUtils.isNotBlank(getAjaxReturnType()) && getAjaxReturnType().equals(
        //        UifConstants.AjaxReturnTypes.UPDATENONE.getKey());
        return StringUtils.isNotBlank(getAjaxReturnType()) && getAjaxReturnType().equals(
                UifConstants.AjaxReturnTypes.UPDATENONE.getKey());
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#isJsonRequest()
     */
    @Override
    public boolean isJsonRequest() {
        return StringUtils.isNotBlank(getRequestJsonTemplate());
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getRequestJsonTemplate()
     */
    @Override
    public String getRequestJsonTemplate() {
        return requestJsonTemplate;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#setRequestJsonTemplate
     */
    @Override
    public void setRequestJsonTemplate(String requestJsonTemplate) {
        this.requestJsonTemplate = requestJsonTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCollectionPagingRequest() {
        return collectionPagingRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCollectionPagingRequest(boolean collectionPagingRequest) {
        this.collectionPagingRequest = collectionPagingRequest;
    }

    /**
     * Returns the String entered by the user when presented a dialog
     *
     * <p>
     * Field defined here so all forms will be able to bind to a dialog using the same property
     * </p>
     *
     * @return String - the text entered by a user as a reply in a modal dialog.
     */
    public String getDialogExplanation() {
        return dialogExplanation;
    }

    /**
     * Sets the dialogExplanation text value.
     *
     * @param dialogExplanation - text entered by user when replying to a modal dialog
     */
    public void setDialogExplanation(String dialogExplanation) {
        this.dialogExplanation = dialogExplanation;
    }

    /**
     * Represents the option chosen by the user when interacting with a modal dialog
     *
     * <p>
     * This is used to determine which option was chosen by the user. The value is the key in the key/value pair
     * selected in the control.
     * </p>
     *
     * @return - String key selected by the user
     */
    public String getDialogResponse() {
        return dialogResponse;
    }

    /**
     * Sets the response key text selected by the user as a response to a modal dialog
     *
     * @param dialogResponse - the key of the option chosen by the user
     */
    public void setDialogResponse(String dialogResponse) {
        this.dialogResponse = dialogResponse;
    }

    /**
     * Gets the DialogManager for this view/form
     *
     * <p>
     * The DialogManager tracks modal dialog interactions with the user
     * </p>
     *
     * @return dialog manager
     */
    public DialogManager getDialogManager() {
        return dialogManager;
    }

    /**
     * Sets the DialogManager for this view
     *
     * @param dialogManager - DialogManager instance for this view
     */
    public void setDialogManager(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    /**
     * @see org.kuali.rice.krad.uif.view.ViewModel#getExtensionData()
     */
    @Override
    public Map<String, Object> getExtensionData() {
        return extensionData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtensionData(Map<String, Object> extensionData) {
        this.extensionData = extensionData;
    }

    /**
     * A generic map for query parameters
     *
     * @return Map<String, String>
     */
    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    /**
     * Setter for the generic query parameters
     *
     * @param queryParameters
     */
    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    /**
     * The {@code List} that contains all newly added items for the collections on the model
     *
     * <p>
     * This list contains the new items for all the collections on the model.
     * </p>
     *
     * @return List of the newly added item lists
     */
    public List getAddedCollectionItems() {
        return addedCollectionItems;
    }

    /**
     * Setter for the newly added item list
     *
     * @param addedCollectionItems
     */
    public void setAddedCollectionItems(List addedCollectionItems) {
        this.addedCollectionItems = addedCollectionItems;
    }

    /**
     * Indicates whether an collection item has been newly added
     *
     * <p>
     * Tests collection items against the list of newly added items on the model. This list gets cleared when the view
     * is submitted and the items are persisted.
     * </p>
     *
     * @param item - the item to test against list of newly added items
     * @return boolean true if the item has been newly added
     */
    public boolean isAddedCollectionItem(Object item) {
        return addedCollectionItems.contains(item);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getClass().getSimpleName() ).append(" [viewId=").append(this.viewId).append(", viewName=").append(this.viewName)
                .append(", viewTypeName=").append(this.viewTypeName).append(", pageId=").append(this.pageId)
                .append(", methodToCall=").append(this.methodToCall).append(", formKey=").append(this.formKey)
                .append(", requestedFormKey=").append(this.requestedFormKey).append("]");
        return builder.toString();
    }
}
