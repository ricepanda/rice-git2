/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kew.removereplace.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.export.ExportDataSet;
import org.kuali.rice.kew.removereplace.RemoveReplaceDocument;
import org.kuali.rice.kew.removereplace.RuleTarget;
import org.kuali.rice.kew.removereplace.WorkgroupTarget;
import org.kuali.rice.kew.removereplace.dao.RemoveReplaceDocumentDAO;
import org.kuali.rice.kew.removereplace.service.RemoveReplaceDocumentService;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.service.RuleService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.service.WorkflowDocument;
import org.kuali.rice.kew.user.WorkflowUserId;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;


public class RemoveReplaceDocumentServiceImpl implements RemoveReplaceDocumentService {

    private RemoveReplaceDocumentDAO dao;

    public void save(RemoveReplaceDocument document) {
	if (document.getDocumentId() == null) {
	    throw new WorkflowRuntimeException("The given document has a null document ID.  Please assign a document ID prior to saving.");
	}
	dao.save(document);
    }

    public RemoveReplaceDocument findById(Long documentId) {
	return dao.findById(documentId);
    }

    public void blanketApprove(RemoveReplaceDocument document, UserSession user, String annotation) {
	save(document);
	try {
	    WorkflowDocument workflowDoc = new WorkflowDocument(user.getPrincipalId(), document.getDocumentId());
	    constructTitle(document, workflowDoc);
	    attachDocumentContent(document, workflowDoc);
	    workflowDoc.blanketApprove(annotation);
	} catch (WorkflowException e) {
	    throw new WorkflowRuntimeException(e);
	}
    }

    public void route(RemoveReplaceDocument document, UserSession user, String annotation) {
	save(document);
	try {
	    WorkflowDocument workflowDoc = new WorkflowDocument(user.getPrincipalId(), document.getDocumentId());
	    constructTitle(document, workflowDoc);
	    attachDocumentContent(document, workflowDoc);
	    workflowDoc.routeDocument(annotation);
	} catch (WorkflowException e) {
	    throw new WorkflowRuntimeException(e);
	}
    }

    protected void constructTitle(RemoveReplaceDocument document, WorkflowDocument workflowDoc) throws WorkflowException {
	Person user = KIMServiceLocator.getPersonService().getPerson(document.getUserWorkflowId());
	StringBuffer title = new StringBuffer();
	if (document.getOperation().equals(RemoveReplaceDocument.REMOVE_OPERATION)) {
	    title.append("Removing " + user.getPrincipalName() + " from ");
	} else if (document.getOperation().equals(RemoveReplaceDocument.REPLACE_OPERATION)) {
	    Person replaceWithUser = KIMServiceLocator.getPersonService().getPerson(document.getReplacementUserWorkflowId());
	    title.append("Replacing " + user.getPrincipalName() + " with " + replaceWithUser.getPrincipalName() + " in ");
	}
	title.append(document.getRuleTargets().size() + " rules and " + document.getWorkgroupTargets().size() + " workgroups");
	workflowDoc.setTitle(title.toString());
    }

    /**
     * Attaches document content to the WorkflowDocument for the given RemoveReplaceDocument.
     */
    protected void attachDocumentContent(RemoveReplaceDocument document, WorkflowDocument workflowDoc) {
	try {
	    Person user = KIMServiceLocator.getPersonService().getPerson(document.getUserWorkflowId());
	    Element rootElement = new Element("removeReplaceUserDocument");
	    Element removeReplaceElement = null;
	    if (document.getOperation().equals(RemoveReplaceDocument.REMOVE_OPERATION)) {
		removeReplaceElement = new Element("remove");
		Element userElement = new Element("user");
		userElement.setText(user.getPrincipalName());
		removeReplaceElement.addContent(userElement);
	    } else if (document.getOperation().equals(RemoveReplaceDocument.REPLACE_OPERATION)) {
		removeReplaceElement = new Element("replace");
		Element userElement = new Element("user");
		userElement.setText(user.getPrincipalName());
		removeReplaceElement.addContent(userElement);
		Element replaceWithElement = new Element("replaceWith");
		Person replaceWithUser = KIMServiceLocator.getPersonService().getPerson(document.getReplacementUserWorkflowId());
		replaceWithElement.setText(replaceWithUser.getPrincipalName());
		removeReplaceElement.addContent(replaceWithElement);
	    } else {
		throw new WorkflowRuntimeException("Invalid remove/replace operation specified: " + document.getOperation());
	    }
	    rootElement.addContent(removeReplaceElement);

	    // add rules
	    List<RuleBaseValues> rules = loadRules(document);
	    if (!rules.isEmpty()) {
		ExportDataSet ruleDataSet = new ExportDataSet();
		ruleDataSet.getRules().addAll(rules);
		Element rulesElement = KEWServiceLocator.getRuleService().export(ruleDataSet);
		removeReplaceElement.addContent(rulesElement);
	    }

	    // add workgroups
	    List<? extends Group> workgroups = loadWorkgroups(document);
	    if (!workgroups.isEmpty()) {
		ExportDataSet workgroupDataSet = new ExportDataSet();
		workgroupDataSet.getGroups().addAll(workgroups);
		if (true) {
			throw new UnsupportedOperationException("TODO: please implement this once we have xml export of groups working in KIM!");
		}
	    }
//		Element workgroupsElement = KEWServiceLocator.getWorkgroupService().export(workgroupDataSet);
//		removeReplaceElement.addContent(workgroupsElement);
//	    }
//	    workflowDoc.setApplicationContent(XmlHelper.jotNode(rootElement));
	} catch (Exception e) {
	    throw new WorkflowRuntimeException(e);
	}
    }

    protected List<? extends Group> loadWorkgroups(RemoveReplaceDocument document) {
	List<Group> workgroups = new ArrayList<Group>();
	for (WorkgroupTarget workgroupTarget : document.getWorkgroupTargets()) {
		Group group = KIMServiceLocator.getIdentityManagementService().getGroup(workgroupTarget.getWorkgroupId());
	    if (group == null) {
	    	throw new WorkflowRuntimeException("Failed to locate workgroup to change with id " + workgroupTarget.getWorkgroupId());
	    }
	    workgroups.add(group);
	}
	return workgroups;
    }

    protected List<RuleBaseValues> loadRules(RemoveReplaceDocument document) {
	List<RuleBaseValues> rules = new ArrayList<RuleBaseValues>();
	for (RuleTarget ruleTarget : document.getRuleTargets()) {
	    RuleBaseValues rule = KEWServiceLocator.getRuleService().findRuleBaseValuesById(ruleTarget.getRuleId());
	    if (rule == null) {
		throw new WorkflowRuntimeException("Failed to locate rule to change with id " + ruleTarget.getRuleId());
	    }
	    rules.add(rule);
	}
	return rules;
    }


    public void finalize(Long documentId) {
	RemoveReplaceDocument document = findById(documentId);

	if (document == null) {
	    throw new WorkflowRuntimeException("Failed to locate the RemoveReplaceDocument with id " + documentId);
	}
	if (StringUtils.isEmpty(document.getUserWorkflowId())) {
	    throw new WorkflowRuntimeException("RemoveReplaceDocument does not have a user id.");
	}

	List<Long> ruleIds = new ArrayList<Long>();
	if (document.getRuleTargets() != null) {
	    for (RuleTarget ruleTarget : document.getRuleTargets()) {
		ruleIds.add(ruleTarget.getRuleId());
	    }
	}

	List<String> workgroupIds = new ArrayList<String>();
	if (document.getWorkgroupTargets() != null) {
	    for (WorkgroupTarget workgroupTarget : document.getWorkgroupTargets()) {
		workgroupIds.add(workgroupTarget.getWorkgroupId());
	    }
	}

	RuleService ruleService = KEWServiceLocator.getRuleService();
	/**
	 * TODO re-implement replacing of group membership using KIM!
	 */
	//WorkgroupRoutingService workgroupRoutingService = KEWServiceLocator.getWorkgroupRoutingService();
	try {
	    if (RemoveReplaceDocument.REPLACE_OPERATION.equals(document.getOperation())) {
		if (StringUtils.isEmpty(document.getReplacementUserWorkflowId())) {
		    throw new WorkflowRuntimeException("Replacement operation was indicated but RemoveReplaceDocument does not have a replacement user id.");
		}
		ruleService.replaceRuleInvolvement(new WorkflowUserId(document.getUserWorkflowId()), new WorkflowUserId(document.getReplacementUserWorkflowId()), ruleIds, documentId);
		//workgroupRoutingService.replaceWorkgroupInvolvement(new WorkflowUserId(document.getUserWorkflowId()), new WorkflowUserId(document.getReplacementUserWorkflowId()), workgroupIds, documentId);
	    } else if (RemoveReplaceDocument.REMOVE_OPERATION.equals(document.getOperation())) {
		ruleService.removeRuleInvolvement(new WorkflowUserId(document.getUserWorkflowId()), ruleIds, documentId);
		//workgroupRoutingService.removeWorkgroupInvolvement(new WorkflowUserId(document.getUserWorkflowId()), workgroupIds, documentId);
	    } else {
		throw new WorkflowRuntimeException("Invalid operation was specified on the RemoveReplaceDocument: " + document.getOperation());
	    }
	} catch (WorkflowException e) {
	    throw new WorkflowRuntimeException(e);
	}
    }

    public void setRemoveReplaceDocumentDAO(RemoveReplaceDocumentDAO dao) {
	this.dao = dao;
    }

}
