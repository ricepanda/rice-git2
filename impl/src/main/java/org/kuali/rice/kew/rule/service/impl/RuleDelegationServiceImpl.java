/*
 * Copyright 2005-2009 The Kuali Foundation
 *
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
package org.kuali.rice.kew.rule.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.kuali.rice.kew.exception.WorkflowServiceErrorException;
import org.kuali.rice.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.rice.kew.export.ExportDataSet;
import org.kuali.rice.kew.rule.RuleDelegation;
import org.kuali.rice.kew.rule.bo.RuleTemplate;
import org.kuali.rice.kew.rule.dao.RuleDelegationDAO;
import org.kuali.rice.kew.rule.service.RuleDelegationService;
import org.kuali.rice.kew.rule.service.RuleTemplateService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.xml.RuleXmlParser;
import org.kuali.rice.kew.xml.export.RuleDelegationXmlExporter;
import org.kuali.rice.kim.bo.Group;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.service.KIMServiceLocator;


/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RuleDelegationServiceImpl implements RuleDelegationService {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(RuleDelegationServiceImpl.class);
	
	private static final String XML_PARSE_ERROR = "general.error.parsexml";
	
    private RuleDelegationDAO dao;

    public List findByDelegateRuleId(Long ruleId) {
        if (ruleId == null) return Collections.EMPTY_LIST;
        return dao.findByDelegateRuleId(ruleId);
    }

    public void save(RuleDelegation ruleDelegation) {
        dao.save(ruleDelegation);
    }

    public void setRuleDelegationDAO(RuleDelegationDAO dao) {
        this.dao = dao;
    }
    public List findAllCurrentRuleDelegations(){
        return dao.findAllCurrentRuleDelegations();
    }
    public void delete(Long ruleDelegationId){
        dao.delete(ruleDelegationId);
    }

    public RuleDelegation findByRuleDelegationId(Long ruleDelegationId){
        return dao.findByRuleDelegationId(ruleDelegationId);
    }

    public List<RuleDelegation> findByResponsibilityId(Long responsibilityId) {
    	return dao.findByResponsibilityIdWithCurrentRule(responsibilityId);
    }

    public List<RuleDelegation> search(String parentRuleBaseVaueId, String parentResponsibilityId,  String docTypeName, Long ruleId, Long ruleTemplateId, String ruleDescription, String groupId, String principalId,
            String delegationType, Boolean activeInd, Map extensionValues, String workflowIdDirective) {
        return dao.search(parentRuleBaseVaueId, parentResponsibilityId, docTypeName, ruleId, ruleTemplateId, ruleDescription, groupId, principalId, delegationType,
                activeInd, extensionValues, workflowIdDirective);
    }

    public List<RuleDelegation> search(String parentRuleBaseVaueId, String parentResponsibilityId,  String docTypeName, String ruleTemplateName, String ruleDescription, String groupId, String principalId,
            Boolean workgroupMember, String delegationType, Boolean activeInd, Map extensionValues, Collection<String> actionRequestCodes) {

        if ( (StringUtils.isEmpty(docTypeName)) &&
                (StringUtils.isEmpty(ruleTemplateName)) &&
                (StringUtils.isEmpty(ruleDescription)) &&
                (StringUtils.isEmpty(groupId)) &&
                (StringUtils.isEmpty(principalId)) &&
                (extensionValues.isEmpty()) &&
                (actionRequestCodes.isEmpty()) ) {
            // all fields are empty
            throw new IllegalArgumentException("At least one criterion must be sent");
        }

        RuleTemplate ruleTemplate = getRuleTemplateService().findByRuleTemplateName(ruleTemplateName);
        Long ruleTemplateId = null;
        if (ruleTemplate != null) {
            ruleTemplateId = ruleTemplate.getRuleTemplateId();
        }

        if ( ( (extensionValues != null) && (!extensionValues.isEmpty()) ) &&
                (ruleTemplateId == null) ) {
            // cannot have extensions without a correct template
            throw new IllegalArgumentException("A Rule Template Name must be given if using Rule Extension values");
        }

        Collection<String> workgroupIds = new ArrayList<String>();
        if (principalId != null) {
            if ( (workgroupMember == null) || (workgroupMember.booleanValue()) ) {
                workgroupIds = getIdentityManagementService().getGroupIdsForPrincipal(principalId);
            } else {
                // user was passed but workgroups should not be parsed... do nothing
            }
        } else if (groupId != null) {
            Group group = KEWServiceLocator.getIdentityHelperService().getGroup(groupId);
            if (group == null) {
                throw new IllegalArgumentException("Group does not exist in for given group id: " + groupId);
            } else  {
                workgroupIds.add(group.getGroupId());
            }
        }

        return dao.search(parentRuleBaseVaueId, parentResponsibilityId, docTypeName, ruleTemplateId, ruleDescription, workgroupIds, principalId,
                delegationType,activeInd, extensionValues, actionRequestCodes);
    }
    
    public void loadXml(InputStream inputStream, String principalId) {
    	RuleXmlParser parser = new RuleXmlParser();
        try {
            parser.parseRuleDelegations(inputStream);
        } catch (Exception e) { //any other exception
            LOG.error("Error loading xml file", e);
            WorkflowServiceErrorException wsee = new WorkflowServiceErrorException("Error loading xml file", new WorkflowServiceErrorImpl("Error loading xml file", XML_PARSE_ERROR));
            wsee.initCause(e);
            throw wsee;
        }
	}

	public Element export(ExportDataSet dataSet) {
		RuleDelegationXmlExporter exporter = new RuleDelegationXmlExporter();
		return exporter.export(dataSet);
	}

	private IdentityManagementService getIdentityManagementService() {
        return (IdentityManagementService)KIMServiceLocator.getIdentityManagementService();
    }

    private RuleTemplateService getRuleTemplateService() {
        return (RuleTemplateService)KEWServiceLocator.getRuleTemplateService();
    }
}
