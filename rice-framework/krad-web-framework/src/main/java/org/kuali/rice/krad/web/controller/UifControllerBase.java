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
package org.kuali.rice.krad.web.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.lookup.LookupUtils;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.service.ModuleService;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.field.AttributeQueryResult;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleRefreshBuild;
import org.kuali.rice.krad.uif.service.ViewService;
import org.kuali.rice.krad.uif.view.DialogManager;
import org.kuali.rice.krad.uif.view.MessageView;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.util.UrlFactory;
import org.kuali.rice.krad.web.form.HistoryFlow;
import org.kuali.rice.krad.web.form.HistoryManager;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.rice.krad.web.form.UifFormManager;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

/**
 * Base controller class for views within the KRAD User Interface Framework.
 *
 * <p>Provides common methods such as:
 *
 * <ul>
 * <li>Authorization methods such as method to call check</li>
 * <li>Preparing the View instance and setup in the returned ModelAndView</li>
 * <li>Add/Delete Line Methods</li>
 * <li>Navigation Methods</li>
 * </ul>
 *
 * All subclass controller methods after processing should call one of the #getUIFModelAndView methods to
 * setup the {@link org.kuali.rice.krad.uif.view.View} and return the {@link org.springframework.web.servlet.ModelAndView}
 * instance.</p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class UifControllerBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(UifControllerBase.class);

    private UrlBasedViewResolver viewResolver;

    /**
     * Create/obtain the model(form) object before it is passed to the Binder/BeanWrapper. This method
     * is not intended to be overridden by client applications as it handles framework setup and session
     * maintenance. Clients should override createInitialForm() instead when they need custom form initialization.
     *
     * @param request the http request that was made
     * @param response the http response object
     */
    @ModelAttribute(value = "KualiForm")
    public UifFormBase initForm(HttpServletRequest request, HttpServletResponse response) {
        // get Uif form manager from session if exists or setup a new one for the session
        UifFormManager uifFormManager = (UifFormManager) request.getSession().getAttribute(UifParameters.FORM_MANAGER);
        if (uifFormManager == null) {
            uifFormManager = new UifFormManager();
            request.getSession().setAttribute(UifParameters.FORM_MANAGER, uifFormManager);
        }

        // add form manager to GlobalVariables for easy reference by other controller methods
        GlobalVariables.setUifFormManager(uifFormManager);

        // create a new form for every request
        UifFormBase requestForm = createInitialForm(request);

        String formKeyParam = request.getParameter(UifParameters.FORM_KEY);
        if (StringUtils.isNotBlank(formKeyParam)) {
            // retrieves the session form and updates the request from with the session transient attributes
            uifFormManager.updateFormWithSession(requestForm, formKeyParam);
        }

        //set the originally requested form key
        String requestedFormKey = request.getParameter(UifParameters.REQUESTED_FORM_KEY);
        if (StringUtils.isNotBlank(requestedFormKey)) {
            requestForm.setRequestedFormKey(requestedFormKey);
        } else {
            requestForm.setRequestedFormKey(formKeyParam);
        }

        //get the initial referer
        String referer = request.getHeader(UifConstants.REFERER);

        //if none, set the no return flag string
        if (StringUtils.isBlank(referer) && StringUtils.isBlank(requestForm.getReturnLocation())) {
            requestForm.setReturnLocation(UifConstants.NO_RETURN);
        } else if (StringUtils.isBlank(requestForm.getReturnLocation())) {
            requestForm.setReturnLocation(referer);
        }

        //get initial request params
        if (requestForm.getInitialRequestParameters() == null) {
            Map<String, String[]> requestParams = new HashMap<String, String[]>();
            Enumeration<String> names = request.getParameterNames();

            while (names != null && names.hasMoreElements()) {
                String name = KRADUtils.stripXSSPatterns(names.nextElement());
                String[] values = KRADUtils.stripXSSPatterns(request.getParameterValues(name));

                requestParams.put(name, values);
            }

            requestParams.remove(UifConstants.UrlParams.LOGIN_USER);
            requestForm.setInitialRequestParameters(requestParams);
        }

        //set the original request url for this view/form
        String requestUrl = KRADUtils.stripXSSPatterns(KRADUtils.getFullURL(request));
        requestForm.setRequestUrl(requestUrl);

        Object historyManager = request.getSession().getAttribute(UifConstants.HistoryFlow.HISTORY_MANAGER);
        String flowKey = request.getParameter(UifConstants.HistoryFlow.FLOW);

        //add history manager and current flowKey to the form
        if (historyManager != null && historyManager instanceof HistoryManager) {
            requestForm.setHistoryManager((HistoryManager) historyManager);
            requestForm.setFlowKey(flowKey);
        }

        // sets the request form in the request for later retrieval
        request.setAttribute(UifConstants.REQUEST_FORM, requestForm);

        return requestForm;
    }

    /**
     * Called to create a new model(form) object when necessary. This usually occurs on the initial request
     * in a conversation (when the model is not present in the session). This method must be
     * overridden when extending a controller and using a different form type than the superclass.
     *
     * @param request - the http request that was made
     */
    protected abstract UifFormBase createInitialForm(HttpServletRequest request);

    /**
     * Default method mapping for cases where the method to call is not passed, calls the start method
     */
    @MethodAccessible
    @RequestMapping()
    public ModelAndView defaultMapping(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        return start(form, request, response);
    }

    /**
     * Initial method called when requesting a new view instance which checks authorization and forwards
     * the view for rendering
     */
    @MethodAccessible
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, HttpServletRequest request,
            HttpServletResponse response) {

        // check view authorization
        // TODO: this needs to be invoked for each request
        if (form.getView() != null) {

            //populate the field default values
            form.setApplyDefaultValues(true);

            String methodToCall = request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER);
            checkViewAuthorization(form, methodToCall);
        }

        return getUIFModelAndView(form);
    }

    /**
     * Invokes the configured {@link org.kuali.rice.krad.uif.view.ViewAuthorizer} to verify the user has access to
     * open the view. An exception is thrown if access has not been granted
     *
     * <p>
     * Note this method is invoked automatically by the controller interceptor for each request
     * </p>
     *
     * @param form - form instance containing the request data
     * @param methodToCall - the request parameter 'methodToCall' which is used to determine the controller
     * method invoked
     */
    public void checkViewAuthorization(UifFormBase form, String methodToCall) throws AuthorizationException {
        // if user session not established we cannnot authorize the view request
        if (GlobalVariables.getUserSession() == null) {
            return;
        }

        Person user = GlobalVariables.getUserSession().getPerson();

        boolean canOpenView = form.getView().getAuthorizer().canOpenView(form.getView(), form, user);
        if (!canOpenView) {
            throw new AuthorizationException(user.getPrincipalName(), "open", form.getView().getId(),
                    "User '" + user.getPrincipalName() + "' is not authorized to open view ID: " +
                            form.getView().getId(), null);
        }
    }

    /**
     * Invoked when a session timeout occurs, default impl does nothing but render the view
     */
    @MethodAccessible
    @RequestMapping(params = "methodToCall=sessionTimeout")
    public ModelAndView sessionTimeout(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        return getUIFModelAndView(form);
    }

    /**
     * Called by the add line action for a new collection line.
     *
     * <p>Method determines which collection the add action was selected for and invokes the view helper
     * service to add the line</p>
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addLine")
    public ModelAndView addLine(@ModelAttribute("KualiForm") final UifFormBase uifForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        final String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_PATH);
        final String selectedCollectionId = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_ID);

        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for add line action, cannot add new line");
        }

        ViewLifecycle.encapsulateLifecycle(uifForm.getView(), uifForm, uifForm.getViewPostMetadata(), null, request,
                response, new Runnable() {
            @Override
            public void run() {
                ViewLifecycle.getHelper().processCollectionAddLine(uifForm, selectedCollectionId,
                        selectedCollectionPath);
            }
        });

        return getUIFModelAndView(uifForm);
    }

    /**
     * Called by the add blank line action for a new collection line
     *
     * <p>
     * Method determines which collection the add action was selected for and invokes the view helper service to
     * add the blank line.
     * </p>
     *
     * @param uifForm - form instance containing the request data
     * @return the  ModelAndView object
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addBlankLine")
    public ModelAndView addBlankLine(@ModelAttribute("KualiForm") final UifFormBase uifForm, HttpServletRequest request,
            HttpServletResponse response) {

        final String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_PATH);
        final String selectedCollectionId = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_ID);

        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for add line action, cannot add new line");
        }

        ViewLifecycle.encapsulateLifecycle(uifForm.getView(), uifForm, uifForm.getViewPostMetadata(), null, request,
                response, new Runnable() {
            @Override
            public void run() {
                ViewLifecycle.getHelper().processCollectionAddBlankLine(uifForm, selectedCollectionId,
                        selectedCollectionPath);
            }
        });

        return getUIFModelAndView(uifForm);
    }

    /**
     * Called by the save line action for a new collection line. Does server side validation and provides hook
     * for client application to persist specific data.
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=saveLine")
    public ModelAndView saveLine(@ModelAttribute("KualiForm") final UifFormBase uifForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        final String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_PATH);
        final String selectedCollectionId = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_ID);

        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for add line action, cannot add new line");
        }

        String selectedLine = uifForm.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        final int selectedLineIndex;
        if (StringUtils.isNotBlank(selectedLine)) {
            selectedLineIndex = Integer.parseInt(selectedLine);
        } else {
            selectedLineIndex = -1;
        }

        if (selectedLineIndex == -1) {
            throw new RuntimeException("Selected line index was not set for delete line action, cannot delete line");
        }

        ViewLifecycle.encapsulateLifecycle(uifForm.getView(), uifForm, uifForm.getViewPostMetadata(), null, request,
                response, new Runnable() {
            @Override
            public void run() {
                ViewLifecycle.getHelper().processCollectionSaveLine(uifForm, selectedCollectionId,
                        selectedCollectionPath, selectedLineIndex);
            }
        });

        return getUIFModelAndView(uifForm);
    }

    /**
     * Called by the delete line action for a model collection. Method
     * determines which collection the action was selected for and the line
     * index that should be removed, then invokes the view helper service to
     * process the action
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=deleteLine")
    public ModelAndView deleteLine(@ModelAttribute("KualiForm") final UifFormBase uifForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        final String selectedCollectionPath = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_PATH);
        final String selectedCollectionId = uifForm.getActionParamaterValue(UifParameters.SELECTED_COLLECTION_ID);

        if (StringUtils.isBlank(selectedCollectionPath)) {
            throw new RuntimeException("Selected collection was not set for delete line action, cannot delete line");
        }

        String selectedLine = uifForm.getActionParamaterValue(UifParameters.SELECTED_LINE_INDEX);
        final int selectedLineIndex;
        if (StringUtils.isNotBlank(selectedLine)) {
            selectedLineIndex = Integer.parseInt(selectedLine);
        } else {
            selectedLineIndex = -1;
        }

        if (selectedLineIndex == -1) {
            throw new RuntimeException("Selected line index was not set for delete line action, cannot delete line");
        }

        ViewLifecycle.encapsulateLifecycle(uifForm.getView(), uifForm, uifForm.getViewPostMetadata(), null, request,
                response, new Runnable() {
            @Override
            public void run() {
                ViewLifecycle.getHelper().processCollectionDeleteLine(uifForm, selectedCollectionId,
                        selectedCollectionPath, selectedLineIndex);
            }
        });

        return getUIFModelAndView(uifForm);
    }

    /**
     * Just returns as if return with no value was selected.
     */
    @RequestMapping(params = "methodToCall=cancel")
    public ModelAndView cancel(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        return back(form, result, request, response);
    }

    /**
     * Attempts to go back by looking at various return mechanisms in HistoryFlow and on the form.  If a back cannot
     * be determined, returns to the application url.
     */
    @RequestMapping(params = "methodToCall=back")
    public ModelAndView back(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        Properties props = new Properties();
        props.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.REFRESH);

        if (StringUtils.isNotBlank(form.getReturnFormKey())) {
            props.put(UifParameters.FORM_KEY, form.getReturnFormKey());
        }

        HistoryFlow historyFlow = form.getHistoryManager().getMostRecentFlowByFormKey(form.getFlowKey(),
                form.getRequestedFormKey());

        String returnUrl = form.getReturnLocation();

        //use history flow return location
        if (historyFlow != null) {
            returnUrl = historyFlow.getFlowReturnPoint();
        }

        //return to start handling
        String returnToStart = form.getActionParamaterValue(UifConstants.HistoryFlow.RETURN_TO_START);
        if (StringUtils.isBlank(returnToStart)) {
            returnToStart = request.getParameter(UifConstants.HistoryFlow.RETURN_TO_START);
        }

        if (StringUtils.isNotBlank(returnToStart) && Boolean.parseBoolean(returnToStart) && historyFlow != null &&
                StringUtils.isNotBlank(historyFlow.getFlowStartPoint())) {
            returnUrl = historyFlow.getFlowStartPoint();
        }

        //return to app url if returnUrl still blank
        if (StringUtils.isBlank(returnUrl) || returnUrl.equals(UifConstants.NO_RETURN)) {
            returnUrl = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY);
        }

        // clear current form from session
        GlobalVariables.getUifFormManager().removeSessionForm(form);

        return performRedirect(form, returnUrl, props);
    }

    /**
     * Invoked to navigate back one page in history.
     *
     * @param form - form object that should contain the history object
     */
    @RequestMapping(params = "methodToCall=returnToPrevious")
    public ModelAndView returnToPrevious(@ModelAttribute("KualiForm") UifFormBase form) {

        return returnToHistory(form, false);
    }

    /**
     * Invoked to navigate back to the first page in history.
     *
     * @param form - form object that should contain the history object
     */
    @RequestMapping(params = "methodToCall=returnToHub")
    public ModelAndView returnToHub(@ModelAttribute("KualiForm") UifFormBase form) {

        return returnToHistory(form, true);
    }

    /**
     * Invoked to navigate back to a history entry. The homeFlag will determine whether navigation
     * will be back to the first or last history entry.
     *
     * @param form - form object that should contain the history object
     * @param homeFlag - if true will navigate back to first entry else will navigate to last entry
     * in the history
     */
    public ModelAndView returnToHistory(UifFormBase form, boolean homeFlag) {
        String returnUrl = form.getReturnLocation();

        if (StringUtils.isBlank(returnUrl) || homeFlag) {
            returnUrl = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY);
        }

        // Add the refresh call
        Properties props = new Properties();
        props.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.REFRESH);

        // clear current form from session
        GlobalVariables.getUifFormManager().removeSessionForm(form);

        return performRedirect(form, returnUrl, props);
    }

    /**
     * Handles menu navigation between view pages
     */
    @MethodAccessible
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=navigate")
    public ModelAndView navigate(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        String pageId = form.getActionParamaterValue(UifParameters.NAVIGATE_TO_PAGE_ID);

        //clear dirty flag, if set
        form.setDirtyForm(false);

        return getUIFModelAndView(form, pageId);
    }

    /**
     * Invoked to refresh a view, generally when returning from another view (for example a lookup))
     */
    @MethodAccessible
    @RequestMapping(params = "methodToCall=refresh")
    public ModelAndView refresh(@ModelAttribute("KualiForm") final UifFormBase form, BindingResult result,
            final HttpServletRequest request, HttpServletResponse response) throws Exception {

        ViewLifecycle.encapsulateLifecycle(form.getView(), form, form.getViewPostMetadata(), null, request, response,
                new ViewLifecycleRefreshBuild());

        return getUIFModelAndView(form);
    }

    /**
     * Builds up a URL to the lookup view based on the given post action
     * parameters and redirects
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=performLookup")
    public ModelAndView performLookup(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        Properties lookupParameters = form.getActionParametersAsProperties();

        String lookupObjectClassName = (String) lookupParameters.get(UifParameters.DATA_OBJECT_CLASS_NAME);
        Class<?> lookupObjectClass = null;
        try {
            lookupObjectClass = Class.forName(lookupObjectClassName);
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to get class for name: " + lookupObjectClassName);
            throw new RuntimeException("Unable to get class for name: " + lookupObjectClassName, e);
        }

        // get form values for the lookup parameter fields
        String lookupParameterString = (String) lookupParameters.get(UifParameters.LOOKUP_PARAMETERS);
        if (lookupParameterString != null) {
            Map<String, String> lookupParameterFields = KRADUtils.getMapFromParameterString(lookupParameterString);
            for (Entry<String, String> lookupParameter : lookupParameterFields.entrySet()) {
                String lookupParameterValue = LookupUtils.retrieveLookupParameterValue(form, request, lookupObjectClass,
                        lookupParameter.getValue(), lookupParameter.getKey());

                if (StringUtils.isNotBlank(lookupParameterValue)) {
                    lookupParameters.put(UifPropertyPaths.LOOKUP_CRITERIA + "['" + lookupParameter.getValue() + "']",
                            lookupParameterValue);
                }
            }

            lookupParameters.remove(UifParameters.LOOKUP_PARAMETERS);
        }

        String baseLookupUrl = (String) lookupParameters.get(UifParameters.BASE_LOOKUP_URL);
        lookupParameters.remove(UifParameters.BASE_LOOKUP_URL);

        // set lookup method to call
        lookupParameters.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.START);
        String autoSearchString = (String) lookupParameters.get(UifParameters.AUTO_SEARCH);
        if (Boolean.parseBoolean(autoSearchString)) {
            lookupParameters.put(UifParameters.METHOD_TO_CALL, UifConstants.MethodToCallNames.SEARCH);
        }

        lookupParameters.put(UifParameters.RETURN_LOCATION, form.getFormPostUrl());
        lookupParameters.put(UifParameters.RETURN_FORM_KEY, form.getFormKey());

        // special check for external object classes
        if (lookupObjectClass != null) {
            ModuleService responsibleModuleService =
                    KRADServiceLocatorWeb.getKualiModuleService().getResponsibleModuleService(lookupObjectClass);
            if (responsibleModuleService != null && responsibleModuleService.isExternalizable(lookupObjectClass)) {
                String lookupUrl = responsibleModuleService.getExternalizableDataObjectLookupUrl(lookupObjectClass,
                        lookupParameters);

                return performRedirect(form, lookupUrl, new Properties());
            }
        }

        return performRedirect(form, baseLookupUrl, lookupParameters);
    }

    /**
     * Checks the form/view against all current and future validations and returns warnings for any validations
     * that fail
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=checkForm")
    public ModelAndView checkForm(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        KRADServiceLocatorWeb.getViewValidationService().validateViewSimulation(form);

        return getUIFModelAndView(form);
    }

    /**
     * Invoked to provide the options for a suggest widget. The valid options are retrieved by the associated
     * <code>AttributeQuery</code> for the field containing the suggest widget. The controller method picks
     * out the query parameters from the request and calls <code>AttributeQueryService</code> to perform the
     * suggest query and prepare the result object that will be exposed with JSON
     */
    @MethodAccessible
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=performFieldSuggest")
    public
    @ResponseBody
    AttributeQueryResult performFieldSuggest(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        // retrieve query fields from request
        Map<String, String> queryParameters = new HashMap<String, String>();
        for (Object parameterName : request.getParameterMap().keySet()) {
            if (parameterName.toString().startsWith(UifParameters.QUERY_PARAMETER + ".")) {
                String fieldName = StringUtils.substringAfter(parameterName.toString(),
                        UifParameters.QUERY_PARAMETER + ".");
                String fieldValue = request.getParameter(parameterName.toString());
                queryParameters.put(fieldName, fieldValue);
            }
        }

        // retrieve id for field to perform query for
        String queryFieldId = request.getParameter(UifParameters.QUERY_FIELD_ID);
        if (StringUtils.isBlank(queryFieldId)) {
            throw new RuntimeException(
                    "Unable to find id for field to perform query on under request parameter name: " +
                            UifParameters.QUERY_FIELD_ID);
        }

        // get the field term to match
        String queryTerm = request.getParameter(UifParameters.QUERY_TERM);
        if (StringUtils.isBlank(queryTerm)) {
            throw new RuntimeException(
                    "Unable to find id for query term value for attribute query on under request parameter name: " +
                            UifParameters.QUERY_TERM);
        }

        // invoke attribute query service to perform the query
        AttributeQueryResult queryResult = KRADServiceLocatorWeb.getAttributeQueryService().performFieldSuggestQuery(
                form.getViewPostMetadata(), queryFieldId, queryTerm, queryParameters);

        return queryResult;
    }

    /**
     * Invoked to execute the <code>AttributeQuery</code> associated with a field given the query parameters
     * found in the request. This controller method picks out the query parameters from the request and calls
     * <code>AttributeQueryService</code> to perform the field query and prepare the result object
     * that will be exposed with JSON. The result is then used to update field values in the UI with client
     * script.
     */
    @MethodAccessible
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=performFieldQuery")
    public
    @ResponseBody
    AttributeQueryResult performFieldQuery(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        // retrieve query fields from request
        Map<String, String> queryParameters = new HashMap<String, String>();
        for (Object parameterName : request.getParameterMap().keySet()) {
            if (parameterName.toString().startsWith(UifParameters.QUERY_PARAMETER + ".")) {
                String fieldName = StringUtils.substringAfter(parameterName.toString(),
                        UifParameters.QUERY_PARAMETER + ".");
                String fieldValue = request.getParameter(parameterName.toString());
                queryParameters.put(fieldName, fieldValue);
            }
        }

        // retrieve id for field to perform query for
        String queryFieldId = request.getParameter(UifParameters.QUERY_FIELD_ID);
        if (StringUtils.isBlank(queryFieldId)) {
            throw new RuntimeException(
                    "Unable to find id for field to perform query on under request parameter name: " +
                            UifParameters.QUERY_FIELD_ID);
        }

        // invoke attribute query service to perform the query
        AttributeQueryResult queryResult = KRADServiceLocatorWeb.getAttributeQueryService().performFieldQuery(
                form.getViewPostMetadata(), queryFieldId, queryParameters);

        return queryResult;
    }

    /**
     * returns whether this dialog has been displayed on the client
     *
     * @param dialogId - the id of the dialog
     * @param form - form instance containing the request data
     * @return boolean - true if dialog has been displayed, false if not
     */
    protected boolean hasDialogBeenDisplayed(String dialogId, UifFormBase form) {
        return (form.getDialogManager().hasDialogBeenDisplayed(dialogId));
    }

    /**
     * returns whether the dialog has already been answered by the user
     *
     * @param dialogId - identifier for the dialog group
     * @param form - form instance containing the request data
     * @return boolean - true if client has already responded to the dialog, false otherwise
     */
    protected boolean hasDialogBeenAnswered(String dialogId, UifFormBase form) {
        return (form.getDialogManager().hasDialogBeenAnswered(dialogId));
    }

    /**
     * Sets the status of the dialog tracking record to indicate that this dialog
     * has not yet been asked or answered
     *
     * @param dialogId - the id of the dialog
     * @param form - form instance containing the request data
     */
    protected void resetDialogStatus(String dialogId, UifFormBase form) {
        form.getDialogManager().resetDialogStatus(dialogId);
    }

    /**
     * Handles a modal dialog interaction with the client user when a @{boolean} response is desired
     *
     * <p>
     * If this modal dialog has not yet been presented to the user, a runtime exception is thrown.   Use the following
     * code in the view controller to ensure the dialog has been displayed and answered:
     * <pre>{@code
     *  DialogManager dm = form.getDialogManager();
     *  if (!dm.hasDialogBeenAnswered(dialogId)) {
     *      return showDialog(dialogId, form, request, response);
     *  }
     *  answer = getBooleanDialogResponse(dialogId, form, request, response);
     * }</pre>
     * </p>
     *
     * <p>
     * If the dialog has already been answered by the user.  The boolean value representing the
     * option chosen by the user is returned back to the calling controller
     * </p>
     *
     * @param dialogId - identifier of the dialog group
     * @param form - form instance containing the request data
     * @param request - the http request
     * @param response - the http response
     * @return boolean - true if user chose affirmative response, false if negative response was chosen
     * @throws RiceRuntimeException when dialog has not been answered.
     */
    protected boolean getBooleanDialogResponse(String dialogId, UifFormBase form, HttpServletRequest request,
            HttpServletResponse response) {
        DialogManager dm = form.getDialogManager();
        if (!dm.hasDialogBeenAnswered(dialogId)) {

            // ToDo: It would be nice if showDialog could be called here and avoid this exception.
            //       This would also remove the need of having to call showDialog explicitly.

            throw new RiceRuntimeException("Dialog has not yet been answered by client. " +
                    "Check that hasDialogBeenAnswered(id) returns true.");
        }

        return dm.wasDialogAnswerAffirmative(dialogId);
    }

    /**
     * Handles a modal dialog interaction with the client user when a @{code String} response is desired
     *
     * <p>
     * If this modal dialog has not yet been presented to the user, a runtime exception is thrown.   Use the following
     * code in the view controller to ensure the dialog has been displayed and answered:
     * <pre>{@code
     *  DialogManager dm = form.getDialogManager();
     *  if (!dm.hasDialogBeenAnswered(dialogId)) {
     *      return showDialog(dialogId, form, request, response);
     *  }
     *  answer = getBooleanDialogResponse(dialogId, form, request, response);
     * }</pre>
     * </p>
     *
     * <p>
     * If the dialog has already been answered by the user.  The string value is the key string of the key/value pair
     * assigned to the button that the user chose.
     * </p>
     *
     * @param dialogId - identifier of the dialog group
     * @param form - form instance containing the request data
     * @param request - the http request
     * @param response - the http response
     * @return the key string of the response button
     * @throws RiceRuntimeException when dialog has not been answered.
     */

    protected String getStringDialogResponse(String dialogId, UifFormBase form, HttpServletRequest request,
            HttpServletResponse response) {
        DialogManager dm = form.getDialogManager();
        if (!dm.hasDialogBeenAnswered(dialogId)) {
            // ToDo: It would be nice if showDialog could be called here and avoid this exception.
            //       This would also remove the need of having to call showDialog explicitly.

            throw new RiceRuntimeException("Dialog has not yet been answered by client. " +
                    "Check that hasDialogBeenAnswered(id) returns true.");
        }

        return dm.getDialogAnswer(dialogId);
    }

    /**
     * Complete the response directly and launch lightbox with dialog content upon returning back to the client. If it
     * is an ajax request then set the ajaxReturnType and set the updateComponentId to the dialogId.
     *
     * <p>
     * Need to build up the view/component properly as we would if we returned normally back to the DispatcherServlet
     * from the controller method.
     * </p>
     *
     * @param dialogId - id of the dialog or group to use as content in the lightbox.
     * @param form - the form associated with the view
     * @param request - the http request
     * @param response - the http response
     * @return will return void.  actually, won't return at all.
     * @throws Exception
     */
    protected ModelAndView showDialog(String dialogId, UifFormBase form, HttpServletRequest request,
            HttpServletResponse response) {
        // js script to invoke lightbox: runs onDocumentReady
        form.setLightboxScript("openLightboxOnLoad('" + dialogId + "');");
        form.getDialogManager().addDialog(dialogId, form.getMethodToCall());

        // if the dialog is being invoked sever side via ajax set the ajaxReturnType to update-dialog
        // and set the updateComponentId to the dialogId
        if (form.isAjaxRequest()) {
            form.setAjaxReturnType(UifConstants.AjaxReturnTypes.UPDATEDIALOG.getKey());
            form.setUpdateComponentId(dialogId);
        }

        return getUIFModelAndView(form);
    }

    /**
     * Common return point for dialogs
     *
     * <p>
     * Determines the user responses to the dialog. Performs dialog management and then redirects to the
     * original contoller method.
     * </p>
     *
     * @param form - current form
     * @param result - binding result
     * @param request - http request
     * @param response - http response
     * @return ModelAndView setup for redirect to original controller methodToCall
     * @throws Exception
     */
    @MethodAccessible
    @RequestMapping(params = "methodToCall=returnFromLightbox")
    public ModelAndView returnFromLightbox(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        String newMethodToCall = "";

        // Save user responses from dialog
        DialogManager dm = form.getDialogManager();
        String dialogId = dm.getCurrentDialogId();
        if (dialogId == null) {
            // may have been invoked by client.
            // TODO:  handle this case (scheduled for 2.2-m3)
            // for now, log WARNING and default to start, can we add a growl?
            newMethodToCall = "start";
        } else {
            dm.setDialogAnswer(dialogId, form.getDialogResponse());
            dm.setDialogExplanation(dialogId, form.getDialogExplanation());
            newMethodToCall = dm.getDialogReturnMethod(dialogId);
            dm.setCurrentDialogId(null);
        }

        // call intended controller method
        Properties props = new Properties();
        props.put(UifParameters.METHOD_TO_CALL, newMethodToCall);
        props.put(UifParameters.VIEW_ID, form.getViewId());
        props.put(UifParameters.FORM_KEY, form.getFormKey());
        props.put(UifParameters.AJAX_REQUEST, "false");

        return performRedirect(form, form.getFormPostUrl(), props);
    }

    /**
     * Builds a <code>ModelAndView</code> instance configured to redirect to the
     * URL formed by joining the base URL with the given URL parameters
     *
     * @param form current form instance
     * @param baseUrl base url to redirect to
     * @param urlParameters properties containing key/value pairs for the url parameters, if null or empty,
     * the baseUrl will be used as the full URL
     * @return ModelAndView configured to redirect to the given URL
     */
    protected ModelAndView performRedirect(UifFormBase form, String baseUrl, Properties urlParameters) {
        String redirectUrl = UrlFactory.parameterizeUrl(baseUrl, urlParameters);

        return performRedirect(form, redirectUrl);
    }

    /**
     * Builds a <code>ModelAndView</code> instance configured to redirect to the given URL
     *
     * @param form current form instance
     * @param redirectUrl URL to redirect to
     * @return ModelAndView configured to redirect to the given URL
     */
    protected ModelAndView performRedirect(UifFormBase form, String redirectUrl) {
        // indicate a redirect is occuring to prevent view processing down the line
        form.setRequestRedirected(true);

        // set the ajaxReturnType on the form this will override the return type requested by the client
        form.setAjaxReturnType(UifConstants.AjaxReturnTypes.REDIRECT.getKey());

        ModelAndView modelAndView;
        if (form.isAjaxRequest()) {
            modelAndView = getUIFModelAndView(form, form.getPageId());
            modelAndView.addObject("redirectUrl", redirectUrl);
        } else {
            modelAndView = new ModelAndView(UifConstants.REDIRECT_PREFIX + redirectUrl);
        }

        return modelAndView;
    }

    /**
     * Builds a message view from the given header and message text then forwards the UIF model and view
     *
     * <p>
     * If an error or other type of interruption occurs during the request processing the controller can
     * invoke this message to display the message to the user. This will abandon the view that was requested
     * and display a view with just the message
     * </p>
     *
     * @param form UIF form instance
     * @param headerText header text for the message view (can be blank)
     * @param messageText text for the message to display
     * @return ModelAndView
     */
    protected ModelAndView getMessageView(UifFormBase form, String headerText, String messageText) {
        // get a new message view
        MessageView messageView = (MessageView) getViewService().getViewById(UifConstants.MESSAGE_VIEW_ID);

        messageView.setHeaderText(headerText);
        messageView.setMessageText(messageText);

        form.setViewId(UifConstants.MESSAGE_VIEW_ID);
        form.setView(messageView);

        return getUIFModelAndView(form);
    }

    /**
     * Retrieve a page defined by the page number parameter for a collection group.
     */
    @MethodAccessible
    @RequestMapping(params = "methodToCall=retrieveCollectionPage")
    public ModelAndView retrieveCollectionPage(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        form.setCollectionPagingRequest(true);

        return getUIFModelAndView(form);
    }

    /**
     * Get method for getting aaData for jquery datatables which are using sAjaxSource option.
     *
     * <p>This will render the aaData JSON for the displayed page of the table matching the tableId passed in the
     * request parameters.</p>
     */
    @MethodAccessible
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=tableJsonRetrieval")
    public ModelAndView tableJsonRetrieval(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        form.setCollectionPagingRequest(true);

        // set property to trigger special JSON rendering logic
        form.setRequestJsonTemplate(UifConstants.TableToolsValues.JSON_TEMPLATE);

        return getUIFModelAndView(form);
    }

    /**
     * Configures the <code>ModelAndView</code> instance containing the form
     * data and pointing to the UIF generic spring view
     *
     * @param form form instance containing the model data
     * @return ModelAndView object with the contained form
     */
    protected ModelAndView getUIFModelAndView(UifFormBase form) {
        return getUIFModelAndView(form, form.getPageId());
    }

    /**
     * Configures the <code>ModelAndView</code> instance containing the form
     * data and pointing to the UIF generic spring view
     *
     * @param form form instance containing the model data
     * @param pageId id of the page within the view that should be rendered, can
     * be left blank in which the current or default page is rendered
     * @return ModelAndView object with the contained form
     */
    protected ModelAndView getUIFModelAndView(UifFormBase form, String pageId) {
        return UifControllerHelper.getUIFModelAndView(form, pageId);
    }

    /**
     * Retrieves a new view instance for the given view id and then configures the <code>ModelAndView</code>
     * instance containing the form data and pointing to the UIF generic spring view
     *
     * @param form form instance containing the model data
     * @param viewId id for the view that should be built
     * @return ModelAndView object with the contained form
     */
    protected ModelAndView getUIFModelAndViewWithInit(UifFormBase form, String viewId) {
        View view = getViewService().getViewById(viewId);

        Assert.notNull(view, "View not found with id: " + viewId);

        form.setView(view);
        form.setViewId(viewId);

        return UifControllerHelper.getUIFModelAndView(form, form.getPageId());
    }

    /**
     * Configures the <code>ModelAndView</code> instance containing the form data and pointing to the UIF
     * generic spring view, additional attributes may be exposed to the view through the map argument
     *
     * @param form form instance containing the model data
     * @param additionalViewAttributes map of additional attributes to expose, key will be string the object
     * is exposed under
     * @return ModelAndView object with the contained form
     */
    protected ModelAndView getUIFModelAndView(UifFormBase form, Map<String, Object> additionalViewAttributes) {
        ModelAndView modelAndView = UifControllerHelper.getUIFModelAndView(form, form.getPageId());

        if (additionalViewAttributes != null) {
            for (Map.Entry<String, Object> additionalViewAttribute : additionalViewAttributes.entrySet()) {
                modelAndView.getModelMap().put(additionalViewAttribute.getKey(), additionalViewAttribute.getValue());
            }
        }

        return modelAndView;
    }

    /**
     * Convenience method for getting an instance of the view service.
     *
     * @return view service implementation
     */
    protected ViewService getViewService() {
        return KRADServiceLocatorWeb.getViewService();
    }

}
