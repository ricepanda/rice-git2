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
package org.kuali.rice.krad.rules.rule.event;

import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.rules.rule.BusinessRule;

import java.util.List;

/**
 * Parent interface of all document-related events, which are used to drive the business rules evaluation process.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface KualiDocumentEvent {

    /**
     * @return Document The document associated with this event
     */
    Document getDocument();

    /**
     * The name of the event.
     *
     * @return String
     */
    String getName();

    /**
     * A description of the event.
     *
     * @return String
     */
    String getDescription();

    /**
     * @return errorPathPrefix for this event
     */
    String getErrorPathPrefix();

    /**
     * Returns the interface that classes must implement to receive this event.
     *
     * @return rule interface
     */
    Class<? extends BusinessRule> getRuleInterfaceClass();

    /**
     * Validates the event has all the necessary properties.
     */
    void validate();

    /**
     * Invokes the event handling method on the rule object.
     *
     * @param rule business rule
     * @return true if the rule matches
     */
    boolean invokeRuleMethod(BusinessRule rule);

    /**
     * This will return a list of events that are spawned from this event.
     *
     * @return list of events
     */
    List<KualiDocumentEvent> generateEvents();
}
