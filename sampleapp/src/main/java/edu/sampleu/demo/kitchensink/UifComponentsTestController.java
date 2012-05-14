/**
 * Copyright 2005-2012 The Kuali Foundation
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
package edu.sampleu.demo.kitchensink;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kuali.rice.krad.datadictionary.validation.ViewAttributeValueReader;
import org.kuali.rice.krad.service.DictionaryValidationService;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.UifParameters;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.field.DataField;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.layout.StackedLayoutManager;
import org.kuali.rice.krad.uif.layout.TableLayoutManager;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.beans.PropertyEditor;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Controller for the Test UI Page
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Controller
@RequestMapping(value = "/uicomponents")
public class UifComponentsTestController extends UifControllerBase {

    /**
     * @see org.kuali.rice.krad.web.controller.UifControllerBase#createInitialForm(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected UifComponentsTestForm createInitialForm(HttpServletRequest request) {
        return new UifComponentsTestForm();
    }

    @Override
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        UifComponentsTestForm uiTestForm = (UifComponentsTestForm) form;

        GlobalVariables.getMessageMap().addGrowlMessage("Welcome!", "kitchenSink.welcome");

        return super.start(uiTestForm, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=save")
    public ModelAndView save(@ModelAttribute("KualiForm") UifComponentsTestForm uiTestForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        KRADServiceLocatorWeb.getViewValidationService().validateView(uiTestForm);
        return getUIFModelAndView(uiTestForm);
    }


    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=close")
    public ModelAndView close(@ModelAttribute("KualiForm") UifComponentsTestForm uiTestForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        return getUIFModelAndView(uiTestForm, "UifCompView-Page1");
    }

    /**
     * Handles menu navigation between view pages
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=navigate")
    public ModelAndView navigate(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        String pageId = form.getActionParamaterValue(UifParameters.NAVIGATE_TO_PAGE_ID);

        if (pageId.equals("UifCompView-Page8")) {
            GlobalVariables.getMessageMap().putError("gField1", "serverTestError");
            GlobalVariables.getMessageMap().putError("gField1", "serverTestError2");
            GlobalVariables.getMessageMap().putError("gField2", "serverTestError");
            GlobalVariables.getMessageMap().putError("gField3", "serverTestError");
            GlobalVariables.getMessageMap().putWarning("gField1", "serverTestWarning");
            GlobalVariables.getMessageMap().putWarning("gField2", "serverTestWarning");
            GlobalVariables.getMessageMap().putInfo("gField2", "serverTestInfo");
            GlobalVariables.getMessageMap().putInfo("gField3", "serverTestInfo");
        }
        // only refreshing page
        form.setRenderFullView(false);

        return getUIFModelAndView(form, pageId);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=refreshProgGroup")
    public ModelAndView refreshProgGroup(@ModelAttribute("KualiForm") UifComponentsTestForm uiTestForm,
            BindingResult result, HttpServletRequest request, HttpServletResponse response) {

        return getUIFModelAndView(uiTestForm);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=refreshWithServerMessages")
    public ModelAndView refreshWithServerMessages(@ModelAttribute("KualiForm") UifComponentsTestForm uiTestForm,
            BindingResult result, HttpServletRequest request, HttpServletResponse response) {
        GlobalVariables.getMessageMap().putError("field45", "serverTestError");
        GlobalVariables.getMessageMap().putWarning("field45", "serverTestWarning");
        GlobalVariables.getMessageMap().putInfo("field45", "serverTestInfo");

        return getUIFModelAndView(uiTestForm);
    }

    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=genCollectionServerMessages")
    public ModelAndView genCollectionServerMessages(@ModelAttribute("KualiForm") UifComponentsTestForm uiTestForm,
            BindingResult result, HttpServletRequest request, HttpServletResponse response) throws Exception {
        GlobalVariables.getMessageMap().putError("list2[0].field1", "serverTestError");
        GlobalVariables.getMessageMap().putWarning("list2[0].field1", "serverTestWarning");
        GlobalVariables.getMessageMap().putInfo("list2[0].field1", "serverTestInfo");

        GlobalVariables.getMessageMap().putError("list3[0].field1", "serverTestError");
        GlobalVariables.getMessageMap().putWarning("list3[0].field1", "serverTestWarning");
        GlobalVariables.getMessageMap().putInfo("list3[0].field1", "serverTestInfo");

        GlobalVariables.getMessageMap().putError("list5[0].subList[0].field1", "serverTestError");
        GlobalVariables.getMessageMap().putWarning("list5[0].subList[0].field1", "serverTestWarning");
        GlobalVariables.getMessageMap().putInfo("list5[0].subList[0].field1", "serverTestInfo");
        return refresh(uiTestForm, result, request, response);
    }

    
    /**
     * Adds errors to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addErrors")
    public ModelAndView addErrors(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageSectionMessages")){
            GlobalVariables.getMessageMap().putError("Demo-ValidationLayout-Section1", "errorSectionTest");
            GlobalVariables.getMessageMap().putError("Demo-ValidationLayout-Section2", "errorSectionTest");
        }
        else if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageUnmatched")){
            GlobalVariables.getMessageMap().putError("badKey", "unmatchedTest");
        }
        
        Map<String, PropertyEditor> propertyEditors = form.getPostedView().getViewIndex().getFieldPropertyEditors();
        for(String key: propertyEditors.keySet()){
            GlobalVariables.getMessageMap().putError(key, "error1Test");
        }
       
        return getUIFModelAndView(form);
    }
    
    /**
     * Adds warnings to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addWarnings")
    public ModelAndView addWarnings(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageSectionMessages")){
            GlobalVariables.getMessageMap().putWarning("Demo-ValidationLayout-Section1", "warningSectionTest");
            GlobalVariables.getMessageMap().putWarning("Demo-ValidationLayout-Section2", "warningSectionTest");
        }
        else if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageUnmatched")){
            GlobalVariables.getMessageMap().putWarning("badKey", "unmatchedTest");
        }

        Map<String, PropertyEditor> propertyEditors = form.getPostedView().getViewIndex().getFieldPropertyEditors();
        for(String key: propertyEditors.keySet()){
            GlobalVariables.getMessageMap().putWarning(key, "warning1Test");
        }

        return getUIFModelAndView(form);
    }
    
    /**
     * Adds infos to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addInfo")
    public ModelAndView addInfo(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageSectionMessages")){
            GlobalVariables.getMessageMap().putInfo("Demo-ValidationLayout-Section1", "infoSectionTest");
            GlobalVariables.getMessageMap().putInfo("Demo-ValidationLayout-Section2", "infoSectionTest");
        }
        else if(form.getPostedView().getCurrentPageId().equals("Demo-ValidationLayout-SectionsPageUnmatched")){
            GlobalVariables.getMessageMap().putInfo("badKey", "unmatchedTest");
        }

        Map<String, PropertyEditor> propertyEditors = form.getPostedView().getViewIndex().getFieldPropertyEditors();
        for(String key: propertyEditors.keySet()){
            GlobalVariables.getMessageMap().putInfo(key, "info1Test");
        }

        return getUIFModelAndView(form);
    }
    
    /**
     * Adds all message types to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addAllMessages")
    public ModelAndView addAllMessages(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        this.addErrors(form, result, request, response);
        this.addWarnings(form, result, request, response);
        this.addInfo(form, result, request, response);

        return getUIFModelAndView(form);
    }

    /**
     * Adds error and warning messages to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addErrorWarnMessages")
    public ModelAndView addErrorWarnMessages(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        this.addErrors(form, result, request, response);
        this.addWarnings(form, result, request, response);

        return getUIFModelAndView(form);
    }

    /**
     * Adds error and info messages to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addErrorInfoMessages")
    public ModelAndView addErrorInfoMessages(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        this.addErrors(form, result, request, response);
        this.addInfo(form, result, request, response);

        return getUIFModelAndView(form);
    }

    /**
     * Adds warning and info messages to fields defined in the validationMessageFields array
     */
    @RequestMapping(method = RequestMethod.POST, params = "methodToCall=addWarningInfoMessages")
    public ModelAndView addWarnInfoMessages(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {

        this.addWarnings(form, result, request, response);
        this.addInfo(form, result, request, response);

        return getUIFModelAndView(form);
    }

}
