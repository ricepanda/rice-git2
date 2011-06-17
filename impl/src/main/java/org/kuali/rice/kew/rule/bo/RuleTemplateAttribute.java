/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.kew.rule.bo;

import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.rice.core.jpa.annotations.Sequence;
import org.kuali.rice.core.reflect.ObjectDefinition;
import org.kuali.rice.core.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kew.bo.KewPersistableBusinessObjectBase;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.rule.RuleExtension;
import org.kuali.rice.kew.rule.RuleValidationAttribute;
import org.kuali.rice.kew.rule.WorkflowAttribute;
import org.kuali.rice.kew.rule.service.RuleAttributeService;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.bo.Inactivateable;


/**
 * A model bean which services as the link between a {@link RuleTemplate} and
 * a {@link RuleAttribute}.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Entity
@Table(name="KREW_RULE_TMPL_ATTR_T")
@Sequence(name="KREW_RTE_TMPL_S", property="ruleTemplateAttributeId")
public class RuleTemplateAttribute extends KewPersistableBusinessObjectBase implements Comparable<RuleTemplateAttribute>, Inactivateable {

    private static final long serialVersionUID = -3580049225424553828L;
    @Id
	@Column(name="RULE_TMPL_ATTR_ID")
	private Long ruleTemplateAttributeId;
    @Column(name="RULE_TMPL_ID", insertable=false, updatable=false)
	private Long ruleTemplateId;
    @Column(name="RULE_ATTR_ID", insertable=false, updatable=false)
	private Long ruleAttributeId;
    @Column(name="REQ_IND")
	private Boolean required;
    @Column(name="ACTV_IND")
	private Boolean active;
    @Column(name="DSPL_ORD")
	private Integer displayOrder;
    @Column(name="DFLT_VAL")
	private String defaultValue;

    @ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="RULE_TMPL_ID")
	private RuleTemplate ruleTemplate;
    @ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="RULE_ATTR_ID")
	private RuleAttribute ruleAttribute;
    @OneToMany(mappedBy="ruleTemplateAttribute")
	private List<RuleExtension> ruleExtensions;
    
    
    public RuleTemplateAttribute() {
	this.required = Boolean.FALSE;
	this.active = Boolean.TRUE;
    }
   
    public int compareTo(RuleTemplateAttribute ruleTemplateAttribute) {
    	if ((this.getDisplayOrder() != null) && (ruleTemplateAttribute.getDisplayOrder() != null)) {
	    	return this.getDisplayOrder().compareTo(ruleTemplateAttribute.getDisplayOrder());
	    }
    	return 0;
    }

    public Object getAttribute() {
	try {
	    ObjectDefinition objectDefinition = new ObjectDefinition(getRuleAttribute().getClassName(), getRuleAttribute().getServiceNamespace());
	    Object attribute = GlobalResourceLoader.getObject(objectDefinition);
	    if (attribute == null) {
		throw new WorkflowRuntimeException("Could not find attribute " + objectDefinition);
	    }
	    if (attribute instanceof WorkflowAttribute) {
		((WorkflowAttribute) attribute).setRequired(required.booleanValue());
	    }
	    return attribute;
	} catch (Exception e) {
	    throw new RuntimeException("Caught error attempting to load attribute class: " + getRuleAttribute().getClassName(), e);
	}
    }

    public boolean isWorkflowAttribute() {
	try {
	    Object attributeObject = getAttribute();//GlobalResourceLoader.getResourceLoader().getObject(new ObjectDefinition(getRuleAttribute().getClassName()));
	    if (attributeObject == null) {
		return false;
	    }
	    Class<?> attributeClass = attributeObject.getClass();
	    return WorkflowAttribute.class.isAssignableFrom(attributeClass);
	} catch (Exception e) {
	    throw new RuntimeException("Caught error attempting to load WorkflowAttribute class: " + getRuleAttribute().getClassName(), e);
	}
    }

    public boolean isRuleValidationAttribute() {
	// just check the type here to avoid having to load the class from the class loader if it's not actually there
	return KEWConstants.RULE_VALIDATION_ATTRIBUTE_TYPE.equals(getRuleAttribute().getType());
    }

    /**
     * Instantiates and returns a new instance of the WorkflowAttribute class configured on this template.
     * The calling code should be sure to call isWorkflowAttribute first to verify the type of this attribute
     * is that of a WorkflowAttribute.  Otherwise a RuntimeException will be thrown.
     */
    public WorkflowAttribute getWorkflowAttribute() {
	try {
	    ObjectDefinition objectDefinition = new ObjectDefinition(getRuleAttribute().getClassName(), getRuleAttribute().getServiceNamespace());
	    WorkflowAttribute workflowAttribute = (WorkflowAttribute) GlobalResourceLoader.getResourceLoader().getObject(objectDefinition);
	    if (workflowAttribute == null) {
		throw new WorkflowRuntimeException("Could not find workflow attribute " + objectDefinition);
	    }
	    workflowAttribute.setRequired(required.booleanValue());
	    return workflowAttribute;
	} catch (Exception e) {
	    throw new RuntimeException("Caught exception instantiating new " + getRuleAttribute().getClassName(), e);
	}
    }

    /**
     * Instantiates and returns a new instance of the RuleValidationAttribute class configured on this template.
     * The calling code should be sure to call isRuleValidationAttribute first to verify the type of this attribute
     * is that of a RuleValidationAttribute.  Otherwise a RuntimeException will be thrown.
     */
    public RuleValidationAttribute getRuleValidationAttribute() {
	try {
	    return (RuleValidationAttribute) getAttribute();
	} catch (Exception e) {
	    throw new RuntimeException("Caught exception instantiating new " + getRuleAttribute().getClassName(), e);
	}
    }

    public List<RuleExtension> getRuleExtensions() {
	return ruleExtensions;
    }

    public void setRuleExtensions(List<RuleExtension> ruleExtensions) {
	this.ruleExtensions = ruleExtensions;
    }

    public RuleAttribute getRuleAttribute() {
	if (ruleAttribute == null && ruleAttributeId != null) {
	    ruleAttribute = ((RuleAttributeService) KEWServiceLocator.getService(KEWServiceLocator.RULE_ATTRIBUTE_SERVICE)).findByRuleAttributeId(ruleAttributeId);
	}
	return ruleAttribute;
    }

    public void setRuleAttribute(RuleAttribute ruleAttribute) {
	this.ruleAttribute = ruleAttribute;
    }

    public RuleTemplate getRuleTemplate() {
	return ruleTemplate;
    }

    public void setRuleTemplate(RuleTemplate ruleTemplate) {
	this.ruleTemplate = ruleTemplate;
    }

    public String getDefaultValue() {
	return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
	this.defaultValue = defaultValue;
    }

    public Integer getDisplayOrder() {
	return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
	this.displayOrder = displayOrder;
    }

    public boolean isRequired() {
        return (getRequired() == null) || (getRequired().booleanValue());
    }

    public Boolean getRequired() {
	return required;
    }

    public void setRequired(Boolean required) {
	this.required = required;
    }

    public boolean isActive() {
        return (getActive() == null) || (getActive().booleanValue());
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public void setActive(boolean active) {
    	this.active = active;
    }

    public Long getRuleAttributeId() {
	return ruleAttributeId;
    }

    public void setRuleAttributeId(Long ruleAttributeId) {
	this.ruleAttributeId = ruleAttributeId;
    }

    public Long getRuleTemplateAttributeId() {
	return ruleTemplateAttributeId;
    }

    public void setRuleTemplateAttributeId(Long ruleTemplateAttributeId) {
	this.ruleTemplateAttributeId = ruleTemplateAttributeId;
    }

    public Long getRuleTemplateId() {
	return ruleTemplateId;
    }

    public void setRuleTemplateId(Long ruleTemplateId) {
	this.ruleTemplateId = ruleTemplateId;
    }

    public Object copy(boolean preserveKeys) {
	RuleTemplateAttribute ruleTemplateAttributeClone = new RuleTemplateAttribute();
	if (defaultValue != null) {
	    ruleTemplateAttributeClone.setDefaultValue(new String(defaultValue));
	}
	if (displayOrder != null) {
	    ruleTemplateAttributeClone.setDisplayOrder(new Integer(displayOrder.intValue()));
	}
	if (required != null) {
	    ruleTemplateAttributeClone.setRequired(new Boolean(required.booleanValue()));
	}
        if (active != null) {
            ruleTemplateAttributeClone.setActive(Boolean.valueOf(active.booleanValue()));
        }
	if (ruleAttribute != null) {
	    ruleTemplateAttributeClone.setRuleAttribute((RuleAttribute) ruleAttribute.copy(preserveKeys));
	}
	if (preserveKeys && ruleTemplateAttributeId != null) {
	    ruleTemplateAttributeClone.setRuleTemplateAttributeId(new Long(ruleTemplateAttributeId.longValue()));
	}
	return ruleTemplateAttributeClone;
    }
    
    @Override
	protected LinkedHashMap<String, Object> toStringMapper() {
		LinkedHashMap<String, Object> propMap = new LinkedHashMap<String, Object>();
	    propMap.put("ruleTemplateAttributeId", getRuleTemplateAttributeId());
	    propMap.put("ruleTemplateId", getRuleTemplateId());
	    propMap.put("ruleAttributeId", getRuleAttributeId());
	    propMap.put("required", getRequired());
	    propMap.put("active", getActive());
	    propMap.put("displayOrder", getDisplayOrder());
	    propMap.put("defaultValue", getDefaultValue());
	    return propMap;
	}
    
}
