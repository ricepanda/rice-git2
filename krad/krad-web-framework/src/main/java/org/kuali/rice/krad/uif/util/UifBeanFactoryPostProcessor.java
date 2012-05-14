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
package org.kuali.rice.krad.uif.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.component.Configurable;
import org.kuali.rice.krad.uif.service.ExpressionEvaluatorService;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Post processes the bean factory to handle UIF property expressions and IDs on inner beans
 *
 * <p>
 * Conditional logic can be implemented with the UIF dictionary by means of property expressions. These are
 * expressions that follow SPEL and can be given as the value for a property using the @{} placeholder. Since such
 * a value would cause an exception when creating the object if the property is a non-string type (value cannot be
 * converted), we need to move those expressions to a Map for processing, and then remove the original property
 * configuration containing the expression. The expressions are then evaluated during the view apply model phase and
 * the result is set as the value for the corresponding property.
 * </p>
 *
 * <p>
 * Spring will not register inner beans with IDs so that the bean definition can be retrieved through the factory,
 * therefore this post processor adds them as top level registered beans
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class UifBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    private static final Log LOG = LogFactory.getLog(UifBeanFactoryPostProcessor.class);

    /**
     * Iterates through all beans in the factory and invokes processing
     *
     * @param beanFactory - bean factory instance to process
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Set<String> processedBeanNames = new HashSet<String>();

        LOG.info("Beginning post processing of bean factory for UIF expressions");

        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            String beanName = beanNames[i];
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            processBeanDefinition(beanName, beanDefinition, beanFactory, processedBeanNames);
        }

        LOG.info("Finished post processing of bean factory for UIF expressions");
    }

    /**
     * If the bean class is type Component, LayoutManager, or BindingInfo, iterate through configured property values
     * and check for expressions
     *
     * <p>
     * If a expression is found for a property, it is added to the 'propertyExpressions' map and then the original
     * property value is removed to prevent binding errors (when converting to a non string type)
     * </p>
     *
     * @param beanName - name of the bean in the factory (only set for top level beans, not nested)
     * @param beanDefinition - bean definition to process for expressions
     * @param beanFactory - bean factory being processed
     */
    protected void processBeanDefinition(String beanName, BeanDefinition beanDefinition,
            ConfigurableListableBeanFactory beanFactory, Set<String> processedBeanNames) {
        Class<?> beanClass = getBeanClass(beanDefinition, beanFactory);
        if ((beanClass == null) || !Configurable.class.isAssignableFrom(beanClass)) {
            return;
        }

        if (processedBeanNames.contains(beanName)) {
            return;
        }

        LOG.debug("Processing bean name '" + beanName + "'");

        MutablePropertyValues pvs = beanDefinition.getPropertyValues();

        if (pvs.getPropertyValue(UifPropertyPaths.PROPERTY_EXPRESSIONS) != null) {
            // already processed so skip (could be reloading dictionary)
            return;
        }

        Map<String, String> propertyExpressions = new ManagedMap<String, String>();
        Map<String, String> parentPropertyExpressions = getPropertyExpressionsFromParent(beanDefinition.getParentName(),
                beanFactory, processedBeanNames);
        boolean parentExpressionsExist = !parentPropertyExpressions.isEmpty();

        // process expressions on property values
        PropertyValue[] pvArray = pvs.getPropertyValues();
        for (PropertyValue pv : pvArray) {
            if (hasExpression(pv.getValue())) {
                // process expression
                String strValue = getStringValue(pv.getValue());
                propertyExpressions.put(pv.getName(), strValue);

                // remove property value so expression will not cause binding exception
                pvs.removePropertyValue(pv.getName());
            } else {
                // process nested objects
                Object newValue = processPropertyValue(pv.getName(), pv.getValue(), parentPropertyExpressions,
                        propertyExpressions, beanFactory, processedBeanNames);
                pvs.removePropertyValue(pv.getName());
                pvs.addPropertyValue(pv.getName(), newValue);
            }

            // removed expression (if exists) from parent map since the property was set on child
            if (parentPropertyExpressions.containsKey(pv.getName())) {
                parentPropertyExpressions.remove(pv.getName());
            }

            // if property is nested, need to override any parent expressions set on nested beans
            if (StringUtils.contains(pv.getName(), ".") && beanDefinition.getParentName() != null) {
                handleExpressionOverridesOnParentNestedBeans(pv.getName(), pv.getValue(), pvs,
                        beanDefinition.getParentName(), beanFactory);
            }
        }

        if (!propertyExpressions.isEmpty() || parentExpressionsExist) {
            // merge two maps
            ManagedMap<String, String> mergedPropertyExpressions = new ManagedMap<String, String>();
            mergedPropertyExpressions.setMergeEnabled(false);
            mergedPropertyExpressions.putAll(parentPropertyExpressions);
            mergedPropertyExpressions.putAll(propertyExpressions);

            pvs.addPropertyValue(UifPropertyPaths.PROPERTY_EXPRESSIONS, mergedPropertyExpressions);
        }

        // if bean name is given and factory does not have it registered we need to add it (inner beans that
        // were given an id)
        if (StringUtils.isNotBlank(beanName) && !StringUtils.contains(beanName, "$") && !StringUtils.contains(beanName,
                "#") && !beanFactory.containsBean(beanName)) {
            ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanName, beanDefinition);
        }

        if (StringUtils.isNotBlank(beanName)) {
            processedBeanNames.add(beanName);
        }
    }

    /**
     * In the cases whether the property name for a child bean is nested and an expression exits on the same property
     * in the parent bean (but is nested in a bean within the parent), special handling is needed to copy the bean
     * definition and remove the expression from the property expressions map
     *
     * @param propertyName - property name configured on child bean for which to find matching parent configuration
     * @param propertyValue - value for the property configuration
     * @param pvs - property values configured on child bean
     * @param parentBeanName - name of the child bean's parent
     * @param beanFactory - factory containing the bean definitions
     */
    protected void handleExpressionOverridesOnParentNestedBeans(String propertyName, Object propertyValue,
            MutablePropertyValues pvs, String parentBeanName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition parentBeanDefinition = beanFactory.getMergedBeanDefinition(parentBeanName);

        MutablePropertyValues parentPvs = parentBeanDefinition.getPropertyValues();
        PropertyValue[] pvArray = parentPvs.getPropertyValues();

        BeanDefinition overrideBeanDefinition = null;
        String overridePropertyPath = null;

        String[] splitPropertyPath = StringUtils.split(propertyName, ".");
        String currentPath = null;
        for (String pathPart : splitPropertyPath) {
            if (currentPath == null) {
                currentPath = pathPart;
            } else {
                currentPath += "." + pathPart;
            }

            if (overridePropertyPath == null) {
                overridePropertyPath = pathPart;
            } else {
                overridePropertyPath += "." + pathPart;
            }

            // continue until we find a matching property value from the parent bean
            if (!parentPvs.contains(currentPath)) {
                continue;
            }

            PropertyValue pv = parentPvs.getPropertyValue(currentPath);
            if ((pv.getValue() instanceof BeanDefinition) || (pv.getValue() instanceof BeanDefinitionHolder)) {
                BeanDefinition propertyBeanDefinition;
                if (pv.getValue() instanceof BeanDefinition) {
                    propertyBeanDefinition = (BeanDefinition) pv.getValue();
                } else {
                    propertyBeanDefinition = ((BeanDefinitionHolder) pv.getValue()).getBeanDefinition();
                }

                // get property expressions from nested bean to check for match against child property
                parentPvs = propertyBeanDefinition.getPropertyValues();
                PropertyValue propertyExpressionsPV = parentPvs.getPropertyValue(UifPropertyPaths.PROPERTY_EXPRESSIONS);
                if (propertyExpressionsPV != null) {
                    Map<String, String> propertyExpressions = new HashMap<String, String>();

                    Object value = propertyExpressionsPV.getValue();
                    if ((value != null) && (value instanceof ManagedMap)) {
                        propertyExpressions.putAll((ManagedMap) value);
                    }

                    String nestedPropertyName = StringUtils.substringAfter(propertyName, overridePropertyPath + ".");
                    if (propertyExpressions.containsKey(nestedPropertyName)) {
                        // found an expression, make a copy of the bean definition for override
                        overrideBeanDefinition = new GenericBeanDefinition(propertyBeanDefinition);

                        // need to make copy of property value with expression removed from map
                        ManagedMap<String, String> copiedPropertyExpressions = new ManagedMap<String, String>();
                        copiedPropertyExpressions.setMergeEnabled(false);
                        copiedPropertyExpressions.putAll(propertyExpressions);
                        copiedPropertyExpressions.remove(nestedPropertyName);

                        overrideBeanDefinition.getPropertyValues().add(UifPropertyPaths.PROPERTY_EXPRESSIONS,
                                copiedPropertyExpressions);

                        // if child property was not expression, add property config to bean (instead of separate 
                        // nested property which would get overridden by the bean)
                        if (!hasExpression(propertyValue)) {
                            overrideBeanDefinition.getPropertyValues().add(nestedPropertyName, propertyValue);
                            pvs.removePropertyValue(propertyName);
                        }

                        break;
                    }
                }

                // no matching expression, continue on checking properties of nested bean
                currentPath = null;
            }
        }

        if (overrideBeanDefinition != null) {
            pvs.addPropertyValue(overridePropertyPath, overrideBeanDefinition);
        }
    }

    /**
     * Retrieves the class for the object that will be created from the bean definition. Since the class might not
     * be configured on the bean definition, but by a parent, each parent bean definition is recursively checked for
     * a class until one is found
     *
     * @param beanDefinition - bean definition to get class for
     * @param beanFactory - bean factory that contains the bean definition
     * @return Class<?> class configured for the bean definition, or null
     */
    protected Class<?> getBeanClass(BeanDefinition beanDefinition, ConfigurableListableBeanFactory beanFactory) {
        if (StringUtils.isNotBlank(beanDefinition.getBeanClassName())) {
            try {
                return Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                // swallow exception and return null so bean is not processed
                return null;
            }
        } else if (StringUtils.isNotBlank(beanDefinition.getParentName())) {
            BeanDefinition parentBeanDefinition = beanFactory.getBeanDefinition(beanDefinition.getParentName());
            if (parentBeanDefinition != null) {
                return getBeanClass(parentBeanDefinition, beanFactory);
            }
        }

        return null;
    }

    /**
     * Retrieves the property expressions map set on the bean with given name. If the bean has not been processed
     * by the bean factory post processor, that is done before retrieving the map
     *
     * @param parentBeanName - name of the parent bean to retrieve map for (if empty a new map will be returned)
     * @param beanFactory - bean factory to retrieve bean definition from
     * @param processedBeanNames - set of bean names that have been processed so far
     * @return Map<String, String> property expressions map from parent or new instance
     */
    protected Map<String, String> getPropertyExpressionsFromParent(String parentBeanName,
            ConfigurableListableBeanFactory beanFactory, Set<String> processedBeanNames) {
        Map<String, String> propertyExpressions = new HashMap<String, String>();
        if (StringUtils.isBlank(parentBeanName) || !beanFactory.containsBeanDefinition(parentBeanName)) {
            return propertyExpressions;
        }

        if (!processedBeanNames.contains(parentBeanName)) {
            processBeanDefinition(parentBeanName, beanFactory.getBeanDefinition(parentBeanName), beanFactory,
                    processedBeanNames);
        }

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(parentBeanName);
        MutablePropertyValues pvs = beanDefinition.getPropertyValues();

        PropertyValue propertyExpressionsPV = pvs.getPropertyValue(UifPropertyPaths.PROPERTY_EXPRESSIONS);
        if (propertyExpressionsPV != null) {
            Object value = propertyExpressionsPV.getValue();
            if ((value != null) && (value instanceof ManagedMap)) {
                propertyExpressions.putAll((ManagedMap) value);
            }
        }

        return propertyExpressions;
    }

    /**
     * Checks whether the given property value is of String type, and if so whether it contains the expression
     * placholder(s)
     *
     * @param propertyValue - value to check for expressions
     * @return boolean true if the property value contains expression(s), false if it does not
     */
    protected boolean hasExpression(Object propertyValue) {
        if (propertyValue != null) {
            // if value is string, check for el expression
            String strValue = getStringValue(propertyValue);
            if (strValue != null) {
                String elPlaceholder = StringUtils.substringBetween(strValue, UifConstants.EL_PLACEHOLDER_PREFIX,
                        UifConstants.EL_PLACEHOLDER_SUFFIX);
                if (StringUtils.isNotBlank(elPlaceholder)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Processes the given property name/value pair for complex objects, such as bean definitions or collections,
     * which if found will be processed for contained property expression values
     *
     * @param propertyName - name of the property whose value is being processed
     * @param propertyValue - value to check
     * @param parentPropertyExpressions - map that holds property expressions for the parent bean definition, used for
     * merging
     * @param propertyExpressions - map that holds property expressions for the bean definition being processed
     * @param beanFactory - bean factory that contains the bean definition being processed
     * @param processedBeanNames - set of bean names that have been processed so far
     * @return Object new value to set for property
     */
    protected Object processPropertyValue(String propertyName, Object propertyValue,
            Map<String, String> parentPropertyExpressions, Map<String, String> propertyExpressions,
            ConfigurableListableBeanFactory beanFactory, Set<String> processedBeanNames) {
        if (propertyValue == null) {
            return null;
        }

        // process nested bean definitions
        if ((propertyValue instanceof BeanDefinition) || (propertyValue instanceof BeanDefinitionHolder)) {
            String beanName = null;
            BeanDefinition beanDefinition;
            if (propertyValue instanceof BeanDefinition) {
                beanDefinition = (BeanDefinition) propertyValue;
            } else {
                beanDefinition = ((BeanDefinitionHolder) propertyValue).getBeanDefinition();
                beanName = ((BeanDefinitionHolder) propertyValue).getBeanName();
            }

            // since overriding the entire bean, clear any expressions from parent that start with the bean property
            removeExpressionsByPrefix(propertyName, parentPropertyExpressions);
            processBeanDefinition(beanName, beanDefinition, beanFactory, processedBeanNames);

            return propertyValue;
        }

        // recurse into collections
        if (propertyValue instanceof Object[]) {
            visitArray(propertyName, parentPropertyExpressions, propertyExpressions, (Object[]) propertyValue,
                    beanFactory, processedBeanNames);
        } else if (propertyValue instanceof List) {
            visitList(propertyName, parentPropertyExpressions, propertyExpressions, (List) propertyValue, beanFactory,
                    processedBeanNames);
        } else if (propertyValue instanceof Set) {
            visitSet(propertyName, parentPropertyExpressions, propertyExpressions, (Set) propertyValue, beanFactory,
                    processedBeanNames);
        } else if (propertyValue instanceof Map) {
            visitMap(propertyName, parentPropertyExpressions, propertyExpressions, (Map) propertyValue, beanFactory,
                    processedBeanNames);
        }

        // others (primitive) just return value as is
        return propertyValue;
    }

    /**
     * Removes entries from the given expressions map whose key starts with the given prefix
     *
     * @param propertyNamePrefix - prefix to search for and remove
     * @param propertyExpressions - map of property expressions to filter
     */
    protected void removeExpressionsByPrefix(String propertyNamePrefix, Map<String, String> propertyExpressions) {
        Map<String, String> adjustedPropertyExpressions = new HashMap<String, String>();
        for (String propertyName : propertyExpressions.keySet()) {
            if (!propertyName.startsWith(propertyNamePrefix)) {
                adjustedPropertyExpressions.put(propertyName, propertyExpressions.get(propertyName));
            }
        }

        propertyExpressions.clear();
        propertyExpressions.putAll(adjustedPropertyExpressions);
    }

    /**
     * Determines whether the given value is of String type and if so returns the string value
     *
     * @param value - object value to check
     * @return String string value for object or null if object is not a string type
     */
    protected String getStringValue(Object value) {
        if (value instanceof TypedStringValue) {
            TypedStringValue typedStringValue = (TypedStringValue) value;
            return typedStringValue.getValue();
        } else if (value instanceof String) {
            return (String) value;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    protected void visitArray(String propertyName, Map<String, String> parentPropertyExpressions,
            Map<String, String> propertyExpressions, Object array, ConfigurableListableBeanFactory beanFactory,
            Set<String> processedBeanNames) {
        Object newArray = null;
        Object[] arrayVal = null;

        boolean isMergeEnabled = false;
        if (array instanceof ManagedArray) {
            isMergeEnabled = ((ManagedList) array).isMergeEnabled();
            arrayVal = (Object[]) ((ManagedList) array).getSource();

            newArray = new ManagedArray(((ManagedList) array).getElementTypeName(), arrayVal.length);
            ((ManagedArray) newArray).setMergeEnabled(isMergeEnabled);
        } else {
            arrayVal = (Object[]) array;
            newArray = new Object[arrayVal.length];
        }

        for (int i = 0; i < arrayVal.length; i++) {
            Object elem = arrayVal[i];
            String elemPropertyName = propertyName + "[" + i + "]";

            if (hasExpression(elem)) {
                String strValue = getStringValue(elem);
                propertyExpressions.put(elemPropertyName, strValue);
                arrayVal[i] = null;
            } else {
                Object newElem = processPropertyValue(elemPropertyName, elem, parentPropertyExpressions,
                        propertyExpressions, beanFactory, processedBeanNames);
                arrayVal[i] = newElem;
            }

            if (isMergeEnabled && parentPropertyExpressions.containsKey(elemPropertyName)) {
                parentPropertyExpressions.remove(elemPropertyName);
            }
        }

        // determine if we need to clear any parent expressions for this list
        if (!isMergeEnabled) {
            // clear any expressions that match the property name minus index
            Map<String, String> adjustedParentExpressions = new HashMap<String, String>();
            for (Map.Entry<String, String> parentExpression : parentPropertyExpressions.entrySet()) {
                if (!parentExpression.getKey().startsWith(propertyName + "[")) {
                    adjustedParentExpressions.put(parentExpression.getKey(), parentExpression.getValue());
                }
            }

            parentPropertyExpressions.clear();
            parentPropertyExpressions.putAll(adjustedParentExpressions);
        }

        if (array instanceof ManagedArray) {
            ((ManagedList) array).setSource(newArray);
        } else {
            array = newArray;
        }
    }

    @SuppressWarnings("unchecked")
    protected void visitList(String propertyName, Map<String, String> parentPropertyExpressions,
            Map<String, String> propertyExpressions, List listVal, ConfigurableListableBeanFactory beanFactory,
            Set<String> processedBeanNames) {
        boolean isMergeEnabled = false;
        if (listVal instanceof ManagedList) {
            isMergeEnabled = ((ManagedList) listVal).isMergeEnabled();
        }

        ManagedList newList = new ManagedList();
        newList.setMergeEnabled(isMergeEnabled);

        for (int i = 0; i < listVal.size(); i++) {
            Object elem = listVal.get(i);
            String elemPropertyName = propertyName + "[" + i + "]";

            if (hasExpression(elem)) {
                String strValue = getStringValue(elem);
                propertyExpressions.put(elemPropertyName, strValue);
                newList.add(i, null);
            } else {
                Object newElem = processPropertyValue(elemPropertyName, elem, parentPropertyExpressions,
                        propertyExpressions, beanFactory, processedBeanNames);
                newList.add(i, newElem);
            }
        }

        // determine if we need to clear any parent expressions for this list
        if (!isMergeEnabled) {
            // clear any expressions that match the property name minus index
            Map<String, String> adjustedParentExpressions = new HashMap<String, String>();
            for (Map.Entry<String, String> parentExpression : parentPropertyExpressions.entrySet()) {
                if (!parentExpression.getKey().startsWith(
                        propertyName + ExpressionEvaluatorService.EMBEDDED_PROPERTY_NAME_ADD_INDICATOR)) {
                    adjustedParentExpressions.put(parentExpression.getKey(), parentExpression.getValue());
                }
            }

            parentPropertyExpressions.clear();
            parentPropertyExpressions.putAll(adjustedParentExpressions);
        }

        listVal.clear();
        listVal.addAll(newList);
    }

    @SuppressWarnings("unchecked")
    protected void visitSet(String propertyName, Map<String, String> parentPropertyExpressions,
            Map<String, String> propertyExpressions, Set setVal, ConfigurableListableBeanFactory beanFactory,
            Set<String> processedBeanNames) {
        boolean isMergeEnabled = false;
        if (setVal instanceof ManagedSet) {
            isMergeEnabled = ((ManagedSet) setVal).isMergeEnabled();
        }

        ManagedSet newSet = new ManagedSet();
        newSet.setMergeEnabled(isMergeEnabled);

        for (Object elem : setVal) {
            if (hasExpression(elem)) {
                String strValue = getStringValue(elem);
                propertyExpressions.put(propertyName + ExpressionEvaluatorService.EMBEDDED_PROPERTY_NAME_ADD_INDICATOR,
                        strValue);
            } else {
                newSet.remove(elem);
                Object newElem = processPropertyValue(propertyName, elem, parentPropertyExpressions,
                        propertyExpressions, beanFactory, processedBeanNames);
                newSet.add(elem);
            }
        }

        // determine if we need to clear any parent expressions for this list
        if (!isMergeEnabled) {
            // clear any expressions that match the property name minus index
            Map<String, String> adjustedParentExpressions = new HashMap<String, String>();
            for (Map.Entry<String, String> parentExpression : parentPropertyExpressions.entrySet()) {
                if (!parentExpression.getKey().startsWith(
                        propertyName + ExpressionEvaluatorService.EMBEDDED_PROPERTY_NAME_ADD_INDICATOR)) {
                    adjustedParentExpressions.put(parentExpression.getKey(), parentExpression.getValue());
                }
            }

            parentPropertyExpressions.clear();
            parentPropertyExpressions.putAll(adjustedParentExpressions);
        }

        setVal.clear();
        setVal.addAll(newSet);
    }

    @SuppressWarnings("unchecked")
    protected void visitMap(String propertyName, Map<String, String> parentPropertyExpressions,
            Map<String, String> propertyExpressions, Map<?, ?> mapVal, ConfigurableListableBeanFactory beanFactory,
            Set<String> processedBeanNames) {
        boolean isMergeEnabled = false;
        if (mapVal instanceof ManagedMap) {
            isMergeEnabled = ((ManagedMap) mapVal).isMergeEnabled();
        }

        ManagedMap newMap = new ManagedMap();
        newMap.setMergeEnabled(isMergeEnabled);

        for (Map.Entry entry : mapVal.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();

            String keyStr = getStringValue(key);
            String elemPropertyName = propertyName + "['" + keyStr + "']";

            if (hasExpression(val)) {
                String strValue = getStringValue(val);
                propertyExpressions.put(elemPropertyName, strValue);
            } else {
                Object newElem = processPropertyValue(elemPropertyName, val, parentPropertyExpressions,
                        propertyExpressions, beanFactory, processedBeanNames);
                newMap.put(key, newElem);
            }

            if (isMergeEnabled && parentPropertyExpressions.containsKey(elemPropertyName)) {
                parentPropertyExpressions.remove(elemPropertyName);
            }
        }

        if (!isMergeEnabled) {
            // clear any expressions that match the property minus key
            Map<String, String> adjustedParentExpressions = new HashMap<String, String>();
            for (Map.Entry<String, String> parentExpression : parentPropertyExpressions.entrySet()) {
                if (!parentExpression.getKey().startsWith(propertyName + "[")) {
                    adjustedParentExpressions.put(parentExpression.getKey(), parentExpression.getValue());
                }
            }

            parentPropertyExpressions.clear();
            parentPropertyExpressions.putAll(adjustedParentExpressions);
        }

        mapVal.clear();
        mapVal.putAll(newMap);
    }
}
