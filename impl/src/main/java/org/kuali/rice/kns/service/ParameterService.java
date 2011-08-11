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
package org.kuali.rice.kns.service;

import java.util.List;
import java.util.Map;

import org.kuali.rice.kns.bo.Parameter;

/**
 * This class provides methods to verify the existence of Parameters, get the value(s), and get ParameterEvaluators. For the most
 * part, your code should be asking for a ParameterEvaluator and interacting with that. For optional parameters (ones that may not
 * exist but should be processed if they do), you will want to use the parameterExists method before using other methods, since an
 * exception will be thrown by the other methods if the referenced parameter does not exist. In some cases you may need to just pull
 * the value(s) of a parameter via the getParameterValue(s) or getIndicatorParameter methods. All of the methods that you will want
 * to use take a Class componentClass and String parameterName argument. Implementations of this class know how to translate these
 * appropriately to retrieve Parameters and construct ParameterEvaluators.
 */
public interface ParameterService {
    /**
     * This method provides an exception free way to ensure that a parameter exists.
     * 
     * @param componentClass
     * @param parameterName
     * @return boolean indicating whether or not the parameter exists
     */
    public boolean parameterExists(Class<? extends Object> componentClass, String parameterName);

    /**
     * This method provides a convenient way to access the a parameter that signifies true or false.
     * 
     * @param componentClass
     * @param parameterName
     * @return boolean value of indicator parameter
     */
    public boolean getIndicatorParameter(Class<? extends Object> componentClass, String parameterName);

    /**
     * This method provides a convenient way to access the a parameter that signifies true or false.
     * 
     * @param namespaceCode
     * @param detailTypeCode
     * @param parameterName
     * @return boolean value of indicator parameter
     */
    public boolean getIndicatorParameter(String namespaceCode, String detailTypeCode, String parameterName);
    
    /**
     * This method returns the actual BusinessObject instance of a parameter.
     * 
     * @param namespaceCode
     * @param detailTypeCode
     * @param parameterName
     * @return The Parameter instance
     */
    public Parameter retrieveParameter(String namespaceCode, 
    		String detailTypeCode, String parameterName);
    
    /**
     * This method returns the unprocessed text value of a parameter.
     * 
     * @param componentClass
     * @param parameterName
     * @return unprocessed string value as a parameter
     */
    public String getParameterValue(Class<? extends Object> componentClass, String parameterName);

    /**
     * This method can be used to derive a value based on another value.
     * 
     * @param componentClass
     * @param parameterName
     * @param constrainingValue
     * @return derived value
     */
    public String getParameterValue(Class<? extends Object> componentClass, String parameterName, String constrainingValue);

    /**
     * This method returns the value of the specified parameter
     * @param namespaceCode
     * @param detailTypeCode 
     * @param parameterName
     */
    public String getParameterValue(String namespaceCode, String detailTypeCode, String parameterName);

    /**
     * This method can be used to parse the value of a parameter.
     * 
     * @param componentClass
     * @param parameterName
     * @return parsed List of String parameter values
     */
    public List<String> getParameterValues(Class<? extends Object> componentClass, String parameterName);

    /**
     * This method can be used to derive a set of values based on another value.
     * 
     * @param componentClass
     * @param parameterName
     * @param constrainingValue
     * @return derived values List<String>
     */
    public List<String> getParameterValues(Class<? extends Object> componentClass, String parameterName, String constrainingValue);

    /**
     * This method returns a list of the parameter values split on implementation specific criteria.
     * For the default KualiConfigurationServiceImpl, the split is on a semi-colon.
     * @param namespaceCode
     * @param detailTypeCode 
     * @param parameterName
     */
    public List<String> getParameterValues(String namespaceCode, String detailTypeCode, String parameterName);
    
    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap a Parameter and provide convenient
     * evaluation methods.
     * 
     * @param componentClass
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(Class<? extends Object> componentClass, String parameterName);

    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap a Parameter and provide convenient
     * evaluation methods.
     * 
     * @param namespaceCode
     * @param detailTypeCode 
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(String namespaceCode, String detailTypeCode, String parameterName);
    
    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap a Parameter and constrainedValue
     * and provide convenient evaluation methods.
     * 
     * @param componentClass
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(Class<? extends Object> componentClass, String parameterName, String constrainedValue);

    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap a Parameter and constrainedValue
     * and provide convenient evaluation methods.
     * 
     * @param namespaceCode
     * @param detailTypeCode 
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(String namespaceCode, String detailTypeCode, String parameterName, String constrainedValue);
    
    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap a Parameter, constrainingValue, and
     * constrainedValue and provide convenient evaluation methods.
     * 
     * @param componentClass
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(Class<? extends Object> componentClass, String parameterName, String constrainingValue, String constrainedValue);

    /**
     * This method will return an instance of a ParameterEvaluator implementation that will wrap an allow Parameter, a deny
     * Parameter, constrainingValue, and constrainedValue and provide convenient evaluation methods.
     * 
     * @param componentClass
     * @param parameterName
     * @return ParameterEvaluator
     */
    public ParameterEvaluator getParameterEvaluator(Class<? extends Object> componentClass, String allowParameterName, String denyParameterName, String constrainingValue, String constrainedValue);

    /**
     * This method can be used to change the value of a Parameter for unit testing purposes.
     * 
     * @param componentClass
     * @param parameterName
     * @param parameterText
     */
    public void setParameterForTesting(Class<? extends Object> componentClass, String parameterName, String parameterText);
    
    /**
     * This method can be used to clear the parameter cache during unit testing.
     */
    public void clearCache();
    
    /**
     * This method can be used to set a namespace.
     * 
     * @param documentOrStepClass
     * 
     */
    public String getNamespace(Class<? extends Object> documentOrStepClass);
    
    /**
     * This method can be used to change the value of a Parameter for unit testing purposes.
     * 
     * @param documentOrStepClass
     */
    public String getDetailType(Class<? extends Object> documentOrStepClass);
    
    /**
     * This method can be used to retrieve a list of parameters that
     * match the given fieldValues criteria. You could also specify the "like"
     * criteria in the Map.
     * 
     * @param   fieldValues The Map containing the key value pairs to be used 
     *                      to build the criteria.
     * @return  List of Parameters that match the criteria.
     */
 	public List<Parameter> retrieveParametersGivenLookupCriteria(Map<String, String> fieldValues);

}
