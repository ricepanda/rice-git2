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
package org.kuali.rice.krad.data.jpa;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.mappings.OneToManyMapping;
import org.eclipse.persistence.mappings.OneToOneMapping;

import java.util.List;

/**
 * Takes a filter generator and executes the changes on the class descriptor for a field.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class Filter {

    /**
     * Takes a list of filter generators and executes the changes on the class descriptor for a field.
     *
     * @param filterGenerators a list of filter generators.
     * @param descriptor the class descriptor to execute the changes on.
     * @param propertyName the property name of the field to change.
     */
    public static void customizeField(List<FilterGenerator> filterGenerators,
            ClassDescriptor descriptor, String propertyName) {

        Expression exp = null;
        ForeignReferenceMapping mapping = null;

        if (OneToOneMapping.class.isAssignableFrom(descriptor.getMappingForAttributeName(propertyName).getClass())) {
            OneToOneMapping databaseMapping = ((OneToOneMapping) descriptor.getMappingForAttributeName(propertyName));
            exp = databaseMapping.buildSelectionCriteria();
            mapping = (ForeignReferenceMapping) databaseMapping;
        } else if (OneToManyMapping.class.isAssignableFrom(descriptor.getMappingForAttributeName(propertyName)
                .getClass())) {
            OneToManyMapping databaseMapping = ((OneToManyMapping) descriptor.getMappingForAttributeName(propertyName));
            exp = databaseMapping.buildSelectionCriteria();
            mapping = (ForeignReferenceMapping) databaseMapping;
        } else {
            throw new RuntimeException("Mapping type not implemented for query customizer for property "+propertyName);
        }

        for (FilterGenerator filterGenerator : filterGenerators) {
            FilterOperators operator = filterGenerator.operator();
            if(!operator.equals(FilterOperators.EQUAL)){
                throw new UnsupportedOperationException("Operator "+operator.getValue()
                        +" not supported in Filter");
            }
            String attributeName = filterGenerator.attributeName();
            Object attributeValue = filterGenerator.attributeValue();
            Class<?> attributeValueClass = filterGenerator.attributeResolverClass();

            if (exp != null && mapping != null) {
                ExpressionBuilder builder = exp.getBuilder();
                if (!attributeValueClass.equals(Void.class)) {
                    try {
                        FilterValue filterValue =
                                (FilterValue) attributeValueClass.newInstance();
                        attributeValue = filterValue.getValue();
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Cannot find query customizer attribute class" + attributeValueClass);
                    }
                }

                if (attributeValue != null) {
                    Expression addedExpression = builder.get(attributeName).equal(attributeValue);
                    exp = exp.and(addedExpression);
                    mapping.setSelectionCriteria(exp);
                }
            }
        }
    }
}
