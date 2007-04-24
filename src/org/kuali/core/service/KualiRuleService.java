/*
 * Copyright 2005-2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.core.service;

import java.util.List;

import org.kuali.core.document.Document;
import org.kuali.core.rule.BusinessRule;
import org.kuali.core.rule.event.KualiDocumentEvent;


/**
 * Defines the interface to the business-rule evaluation service, used to evauluate document-type-specific business rules using
 * document-related events to drive the process.
 */
public interface KualiRuleService {

    /**
     * Retrieves and instantiates the businessRulesClass associated with the event's document type (if any), and calls the
     * appropriate process* method of that businessRule for handling the given event type. This is a helper method that takes in the
     * generic KualiDocumentEvent class and determines which event call to make.
     * 
     * @param event
     * @return true if no rule is applied, or all rules are applied successfully, false otherwise
     */
    public boolean applyRules(KualiDocumentEvent event);

    /**
     * Builds a list containing ad hoc route person events appropriate for the context.
     * 
     * @param document
     * @return List
     */
    public List generateAdHocRoutePersonEvents(Document document);

    /**
     * Builds a list containing ad hoc route workgroup events appropriate for the context.
     * 
     * @param document
     * @return List
     */
    public List generateAdHocRouteWorkgroupEvents(Document document);

    /**
     * Allows code in actions or business objects to directly access rule methods in the class.
     * 
     * @param document
     * @param ruleInterface
     * @return BusinessRule
     */
    public BusinessRule getBusinessRulesInstance(Document document, Class ruleInterface);
}