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
package org.kuali.rice.krad.web.bind;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.kuali.rice.krad.data.DataObjectService;
import org.kuali.rice.krad.data.DataObjectWrapper;
import org.kuali.rice.krad.data.KradDataServiceLocator;
import org.kuali.rice.krad.data.util.Link;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifConstants.ViewType;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.service.ViewService;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Override of ServletRequestDataBinder in order to hook in the UifBeanPropertyBindingResult
 * which instantiates a custom BeanWrapperImpl, and to initialize the view.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class UifServletRequestDataBinder extends ServletRequestDataBinder {

    protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            UifServletRequestDataBinder.class);

    private UifBeanPropertyBindingResult bindingResult;
    private ConversionService conversionService;
    private DataObjectService dataObjectService;
    private boolean changeTracking = false;
    private boolean autoLinking = true;

    public UifServletRequestDataBinder(Object target) {
        super(target);
        this.changeTracking = determineChangeTracking(target);
        setBindingErrorProcessor(new UifBindingErrorProcessor());
    }

    public UifServletRequestDataBinder(Object target, String name) {
        super(target, name);
        this.changeTracking = determineChangeTracking(target);
        setBindingErrorProcessor(new UifBindingErrorProcessor());
    }

    /**
     * Return true if the target of this data binder has change tracking enabled.
     */
    private static boolean determineChangeTracking(Object target) {
        ChangeTracking changeTracking = AnnotationUtils.findAnnotation(target.getClass(), ChangeTracking.class);
        if (changeTracking != null && changeTracking.enabled()) {
            return true;
        }
        return false;
    }

    /**
     * Allows for a custom binding result class.
     *
     * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
     */
    @Override
    public void initBeanPropertyAccess() {
        Assert.state(this.bindingResult == null,
                "DataBinder is already initialized - call initBeanPropertyAccess before other configuration methods");

        this.bindingResult = new UifBeanPropertyBindingResult(getTarget(), getObjectName(), isAutoGrowNestedPaths(),
                getAutoGrowCollectionLimit());
        this.bindingResult.setChangeTracking(this.changeTracking);

        if (this.conversionService != null) {
            this.bindingResult.initConversion(this.conversionService);
        }

        if (this.dataObjectService == null) {
            this.dataObjectService = KradDataServiceLocator.getDataObjectService();
        }
    }

    /**
     * Allows for the setting attributes to use to find the data dictionary data from Kuali
     *
     * @see org.springframework.validation.DataBinder#getInternalBindingResult()
     */
    @Override
    protected AbstractPropertyBindingResult getInternalBindingResult() {
        if (this.bindingResult == null) {
            initBeanPropertyAccess();
        }

        return this.bindingResult;
    }

    /**
     * Disallows direct field access for Kuali
     *
     * @see org.springframework.validation.DataBinder#initDirectFieldAccess()
     */
    @Override
    public void initDirectFieldAccess() {
        LOG.error("Direct Field access is not allowed in UifServletRequestDataBinder.");
        throw new RuntimeException("Direct Field access is not allowed in Kuali");
    }

    /**
     * Helper method to facilitate calling super.bind() from {@link #bind(ServletRequest)}.
     */
    private void _bind(ServletRequest request) {
        super.bind(request);
    }

    /**
     * Calls {@link org.kuali.rice.krad.web.form.UifFormBase#preBind(HttpServletRequest)}, Performs data binding
     * from servlet request parameters to the form, initializes view object, then calls
     * {@link org.kuali.rice.krad.web.form.UifFormBase#postBind(javax.servlet.http.HttpServletRequest)}
     *
     * <p>
     * The view is initialized by first looking for the {@code viewId} parameter in the request. If found, the view is
     * retrieved based on this id. If the id is not present, then an attempt is made to find a view by type. In order
     * to retrieve a view based on type, the view request parameter {@code viewTypeName} must be present. If all else
     * fails and the viewId is populated on the form (could be populated from a previous request), this is used to
     * retrieve the view.
     * </p>
     *
     * @param request - HTTP Servlet Request instance
     */
    @Override
    public void bind(ServletRequest request) {
        UifFormBase form = (UifFormBase) UifServletRequestDataBinder.this.getTarget();

        form.preBind((HttpServletRequest) request);

        _bind(request);

        executeAutomaticLinking(request, form);
        
        if (!form.isUpdateNoneRequest()) {
            // attempt to retrieve a view by unique identifier first, either as request attribute or parameter
            String viewId = (String) request.getAttribute(UifParameters.VIEW_ID);
            if (StringUtils.isBlank(viewId)) {
                viewId = request.getParameter(UifParameters.VIEW_ID);
            }

            View view = null;
            if (StringUtils.isNotBlank(viewId)) {
                view = getViewService().getViewById(viewId);
            }

            // attempt to get view instance by type parameters
            if (view == null) {
                view = getViewByType(request, form);
            }

            // if view not found attempt to find one based on the cached form
            if (view == null) {
                view = getViewFromPreviousModel(form);

                if (view != null) {
                    LOG.warn("Obtained viewId from cached form, this may not be safe!");
                }
            }

            if (view != null) {
                form.setViewId(view.getId());

            } else {
                form.setViewId(null);
            }

            form.setView(view);
        }

        // invoke form callback for custom binding
        form.postBind((HttpServletRequest) request);
    }

    // TODO document me...
    protected void executeAutomaticLinking(ServletRequest request, UifFormBase form) {
        if (!changeTracking) {
            LOG.info("Skip automatic linking because change tracking not enabled for this form.");
            return;
        }
        if (!autoLinking) {
            LOG.info("Skip automatic linking because it has been disabled for this form");
            return;
        }
        Set<String> autoLinkingPaths = determineRootAutoLinkingPaths(form.getClass(), null, new HashSet<Class<?>>());
        List<AutoLinkTarget> targets = extractAutoLinkTargets(autoLinkingPaths);
        // perform linking for each target
        for (AutoLinkTarget target : targets) {
            if (!dataObjectService.supports(target.getTarget().getClass())) {
                LOG.warn("Encountered an auto linking target that is not a valid data object: "
                        + target.getTarget().getClass());
            } else {
                DataObjectWrapper<?> wrapped = dataObjectService.wrap(target.getTarget());
                wrapped.linkChanges(target.getModifiedPropertyPaths());
            }
        }
    }

    protected Set<String> determineRootAutoLinkingPaths(Class<?> rootObjectType, String path, Set<Class<?>> scanned) {
        Set<String> autoLinkingPaths = new HashSet<String>();
        if (scanned.contains(rootObjectType)) {
            return autoLinkingPaths;
        } else {
            scanned.add(rootObjectType);
        }
        Link autoLink = AnnotationUtils.findAnnotation(rootObjectType, Link.class);
        if (autoLink != null && autoLink.cascade()) {
            autoLinkingPaths.addAll(assembleAutoLinkingPaths(path, autoLink));
        } else if (autoLink == null) {
            Field[] fields = FieldUtils.getAllFields(rootObjectType);
            for (Field field : fields) {
                autoLink = field.getAnnotation(Link.class);
                if (autoLink != null) {
                    if (autoLink.cascade()) {
                        String fieldPath = appendToPath(path, field.getName());
                        autoLinkingPaths.addAll(assembleAutoLinkingPaths(fieldPath, autoLink));
                    }
                } else {
                    autoLinkingPaths.addAll(determineRootAutoLinkingPaths(field.getType(), appendToPath(path,
                            field.getName()), scanned));
                }
            }
        }
        return autoLinkingPaths;
    }

    protected Set<String> assembleAutoLinkingPaths(String path, Link autoLink) {
        Set<String> autoLinkingPaths = new HashSet<String>();
        if (ArrayUtils.isEmpty(autoLink.path())) {
            autoLinkingPaths.add(path);
        } else {
            for (String autoLinkingPath : autoLink.path()) {
                autoLinkingPaths.add(appendToPath(path, autoLinkingPath));
            }
        }
        return autoLinkingPaths;
    }

    protected List<AutoLinkTarget> extractAutoLinkTargets(Set<String> autoLinkingPaths) {
        List<AutoLinkTarget> targets = new ArrayList<AutoLinkTarget>();
        for (String autoLinkingPath : autoLinkingPaths) {
            Set<String> modifiedAutoLinkingPaths = new HashSet<String>();
            Set<String> modifiedPaths = ((UifBeanPropertyBindingResult)getInternalBindingResult()).getModifiedPaths();
            for (String modifiedPath : modifiedPaths) {
                if (modifiedPath.startsWith(autoLinkingPath)) {
                    modifiedAutoLinkingPaths.add(modifiedPath.substring(autoLinkingPath.length() + 1));
                }
            }
            Object targetObject = getInternalBindingResult().getPropertyAccessor().getPropertyValue(autoLinkingPath);
            if (targetObject != null) {
                targets.add(new AutoLinkTarget(targetObject, modifiedAutoLinkingPaths));
            }
        }
        return targets;
    }

    private String appendToPath(String path, String pathElement) {
        if (StringUtils.isEmpty(path)) {
            return pathElement;
        } else if (StringUtils.isEmpty(pathElement)) {
            return path;
        }
        return path + "." + pathElement;
    }

    /**
     * Attempts to get a view instance by looking for a view type name in the request or the form and querying
     * that view type with the request parameters
     *
     * @param request request instance to pull parameters from
     * @param form form instance to pull values from
     * @return View instance if found or null
     */
    protected View getViewByType(ServletRequest request, UifFormBase form) {
        View view = null;

        String viewTypeName = request.getParameter(UifParameters.VIEW_TYPE_NAME);
        ViewType viewType = StringUtils.isBlank(viewTypeName) ? form.getViewTypeName() : ViewType.valueOf(viewTypeName);

        if (viewType != null) {
            Map<String, String> parameterMap = KRADUtils.translateRequestParameterMap(request.getParameterMap());
            view = getViewService().getViewByType(viewType, parameterMap);
        }

        return view;
    }

    /**
     * Attempts to get a view instance based on the view id stored on the form (which might not be populated
     * from the request but remaining from session)
     *
     * @param form form instance to pull view id from
     * @return View instance associated with form's view id or null if id or view not found
     */
    protected View getViewFromPreviousModel(UifFormBase form) {
        // maybe we have a view id from the session form
        if (form.getViewId() != null) {
            return getViewService().getViewById(form.getViewId());
        }

        return null;
    }

    public boolean isChangeTracking() {
        return changeTracking;
    }

    public boolean isAutoLinking() {
        return autoLinking;
    }

    public void setAutoLinking(boolean autoLinking) {
        this.autoLinking = autoLinking;
    }

    public ViewService getViewService() {
        return KRADServiceLocatorWeb.getViewService();
    }

    public DataObjectService getDataObjectService() {
        return this.dataObjectService;
    }

    public void setDataObjectService(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    // TODO document me
    private static final class AutoLinkTarget {

        private final Object target;
        private final Set<String> modifiedPropertyPaths;

        AutoLinkTarget(Object target, Set<String> modifiedPropertyPaths) {
            this.target = target;
            this.modifiedPropertyPaths = modifiedPropertyPaths;
        }

        Object getTarget() {
            return target;
        }

        Set<String> getModifiedPropertyPaths() {
            return Collections.unmodifiableSet(modifiedPropertyPaths);
        }

    }

}


