/*
 * Copyright 2005-2007 The Kuali Foundation
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
package org.kuali.rice.kns.service.impl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.bo.BusinessObjectRelationship;
import org.kuali.rice.kns.bo.ExternalizableBusinessObject;
import org.kuali.rice.kns.bo.PersistableBusinessObject;
import org.kuali.rice.kns.dao.BusinessObjectDao;
import org.kuali.rice.kns.exception.ObjectNotABusinessObjectRuntimeException;
import org.kuali.rice.kns.exception.ReferenceAttributeDoesntExistException;
import org.kuali.rice.kns.service.BusinessObjectMetaDataService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.ModuleService;
import org.kuali.rice.kns.service.PersistenceService;
import org.kuali.rice.kns.service.PersistenceStructureService;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is the service implementation for the BusinessObjectService structure. This is the default implementation, that is
 * delivered with Kuali.
 */

public class BusinessObjectServiceImpl implements BusinessObjectService {

    private PersistenceService persistenceService;
    private PersistenceStructureService persistenceStructureService;
    private BusinessObjectDao businessObjectDao;
    private PersonService personService;
    private BusinessObjectMetaDataService businessObjectMetaDataService;

    private boolean illegalBusinessObjectsForSaveInitialized = false;
    private Set<String> illegalBusinessObjectsForSave = new HashSet<String>();
    
    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#save(org.kuali.bo.BusinessObject)
     */
    @Transactional
    public void save(PersistableBusinessObject bo) {
    	validateBusinessObjectForSave(bo);
        businessObjectDao.save(bo);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#save(java.util.List)
     */
    @Transactional
    public void save(List<? extends PersistableBusinessObject> businessObjects) {
        validateBusinessObjectForSave(businessObjects);
        businessObjectDao.save(businessObjects);
    }

    /**
     * 
     * @see org.kuali.rice.kns.service.BusinessObjectService#linkAndSave(org.kuali.rice.kns.bo.BusinessObject)
     */
    @Transactional
    public void linkAndSave(PersistableBusinessObject bo) {
    	validateBusinessObjectForSave(bo);
        persistenceService.linkObjects(bo);
        businessObjectDao.save(bo);
    }

    /**
     * 
     * @see org.kuali.rice.kns.service.BusinessObjectService#linkAndSave(java.util.List)
     */
    @Transactional
    public void linkAndSave(List<? extends PersistableBusinessObject> businessObjects) {
        validateBusinessObjectForSave(businessObjects);
        businessObjectDao.save(businessObjects);
    }

    /**
     * Validates that the 
     * This method ...
     * 
     * @param bo
     */
    protected void validateBusinessObjectForSave(PersistableBusinessObject bo) {
    	if (bo == null) {
            throw new IllegalArgumentException("Object passed in is null");
        }
        if (!isBusinessObjectAllowedForSave(bo)) {
        	throw new IllegalArgumentException("Object passed in is a BusinessObject but has been restricted from save operations according to configuration parameter '" + KNSConstants.Config.ILLEGAL_BUSINESS_OBJECTS_FOR_SAVE);
        }
    }
    
    protected void validateBusinessObjectForSave(List<? extends PersistableBusinessObject> businessObjects) {
    	for (PersistableBusinessObject bo : businessObjects) {
    		 if (bo == null) {
                 throw new IllegalArgumentException("One of the objects in the List is null.");
             }
    		 if (!isBusinessObjectAllowedForSave(bo)) {
    			 throw new IllegalArgumentException("One of the objects in the List is a BusinessObject but has been restricted from save operations according to configuration parameter '" + KNSConstants.Config.ILLEGAL_BUSINESS_OBJECTS_FOR_SAVE
    					 + "  Passed in type was '" + bo.getClass().getName() + "'.");
    		 }
    	}
    }
    
    
    /**
     * Returns true if the BusinessObjectService should be permitted to save instances of the given PersistableBusinessObject.
     * Implementation checks a configuration parameter for class names of PersistableBusinessObjects that shouldn't be allowed
     * to be saved.
     */
    protected boolean isBusinessObjectAllowedForSave(PersistableBusinessObject bo) {
    	if (!illegalBusinessObjectsForSaveInitialized) {
    		synchronized (this) {
    			boolean applyCheck = true;
    			String applyCheckValue = ConfigContext.getCurrentContextConfig().getProperty(KNSConstants.Config.APPLY_ILLEGAL_BUSINESS_OBJECT_FOR_SAVE_CHECK);
    			if (!StringUtils.isEmpty(applyCheckValue)) {
    				applyCheck = new Boolean(applyCheckValue);
    			}
    			if (applyCheck) {
    				String illegalBos = ConfigContext.getCurrentContextConfig().getProperty(KNSConstants.Config.ILLEGAL_BUSINESS_OBJECTS_FOR_SAVE);
    				if (!StringUtils.isEmpty(illegalBos)) {
    					String[] illegalBosSplit = illegalBos.split(",");
    					for (String illegalBo : illegalBosSplit) {
    						illegalBusinessObjectsForSave.add(illegalBo.trim());
    					}
    				}
    			}
    		}
    		illegalBusinessObjectsForSaveInitialized = true;
    	}
    	return !illegalBusinessObjectsForSave.contains(bo.getClass().getName());
    }
    
    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#findByPrimaryKey(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
	public <T> T findBySinglePrimaryKey(Class<T> clazz, Object primaryKey) {
		return (T)businessObjectDao.findBySinglePrimaryKey(clazz, primaryKey);
	}

	/**
     * @see org.kuali.rice.kns.service.BusinessObjectService#findByPrimaryKey(java.lang.Class, java.util.Map)
     */
    public PersistableBusinessObject findByPrimaryKey(Class clazz, Map primaryKeys) {
        return businessObjectDao.findByPrimaryKey(clazz, primaryKeys);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#retrieve(java.lang.Object)
     */
    public PersistableBusinessObject retrieve(PersistableBusinessObject object) {
        return businessObjectDao.retrieve(object);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#findAll(java.lang.Class)
     */
    public Collection findAll(Class clazz) {
        Collection coll = businessObjectDao.findAll(clazz);
        return new ArrayList(coll);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#findMatching(java.lang.Class, java.util.Map)
     */
    public Collection findMatching(Class clazz, Map fieldValues) {
        return new ArrayList(businessObjectDao.findMatching(clazz, fieldValues));
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#countMatching(java.lang.Class, java.util.Map)
     */
    public int countMatching(Class clazz, Map fieldValues) {
        return businessObjectDao.countMatching(clazz, fieldValues);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#countMatching(java.lang.Class, java.util.Map, java.util.Map)
     */
    public int countMatching(Class clazz, Map positiveFieldValues, Map negativeFieldValues) {
        return businessObjectDao.countMatching(clazz, positiveFieldValues, negativeFieldValues);
    }
    
    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#findMatchingOrderBy(java.lang.Class, java.util.Map)
     */
    public Collection findMatchingOrderBy(Class clazz, Map fieldValues, String sortField, boolean sortAscending) {
        return new ArrayList(businessObjectDao.findMatchingOrderBy(clazz, fieldValues, sortField, sortAscending));
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#delete(org.kuali.bo.BusinessObject)
     */
    @Transactional
    public void delete(PersistableBusinessObject bo) {
        businessObjectDao.delete(bo);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#delete(java.util.List)
     */
    @Transactional
    public void delete(List<? extends PersistableBusinessObject> boList) {
        businessObjectDao.delete(boList);
    }


    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#deleteMatching(java.lang.Class, java.util.Map)
     */
    @Transactional
    public void deleteMatching(Class clazz, Map fieldValues) {
        businessObjectDao.deleteMatching(clazz, fieldValues);
    }

    /**
     * @see org.kuali.rice.kns.service.BusinessObjectService#getReferenceIfExists(org.kuali.rice.kns.bo.BusinessObject, java.lang.String)
     */
    public BusinessObject getReferenceIfExists(BusinessObject bo, String referenceName) {

        PersistableBusinessObject referenceBo = null;
        boolean allFkeysHaveValues = true;

        // if either argument is null, then we have nothing to do, complain and abort
        if (ObjectUtils.isNull(bo)) {
            throw new IllegalArgumentException("Passed in BusinessObject was null.  No processing can be done.");
        }
        if (StringUtils.isEmpty(referenceName)) {
            throw new IllegalArgumentException("Passed in referenceName was empty or null.  No processing can be done.");
        }

        // make sure the attribute exists at all, throw exception if not
        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = PropertyUtils.getPropertyDescriptor(bo, referenceName);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (propertyDescriptor == null) {
            throw new ReferenceAttributeDoesntExistException("Requested attribute: '" + referenceName + "' does not exist " + "on class: '" + bo.getClass().getName() + "'. GFK");
        }

        // get the class of the attribute name
        Class referenceClass = ObjectUtils.getPropertyType( bo, referenceName, persistenceStructureService );
        if ( referenceClass == null ) {
        	referenceClass = propertyDescriptor.getPropertyType();
        }

        /*
         * check for Person or EBO references in which case we can just get the reference through propertyutils
         */
        if (ExternalizableBusinessObject.class.isAssignableFrom(referenceClass)) {
            try {
            	BusinessObject referenceBoExternalizable = (BusinessObject) PropertyUtils.getProperty(bo, referenceName);
            	if(referenceBoExternalizable!=null)
            		return referenceBoExternalizable;
            } catch (Exception ex) {
                //throw new RuntimeException("Unable to get property " + referenceName + " from a BO of class: " + bo.getClass().getName(),ex);
            	//Proceed further - get the BO relationship using responsible module service and proceed further
            }
        }

        // make sure the class of the attribute descends from BusinessObject,
        // otherwise throw an exception
        if (!ExternalizableBusinessObject.class.isAssignableFrom(referenceClass) && !PersistableBusinessObject.class.isAssignableFrom(referenceClass)) {
            throw new ObjectNotABusinessObjectRuntimeException("Attribute requested (" + referenceName + ") is of class: " + "'" + referenceClass.getName() + "' and is not a " + "descendent of PersistableBusinessObject.  Only descendents of PersistableBusinessObject " + "can be used.");
        }

        // get the list of foreign-keys for this reference. if the reference
        // does not exist, or is not a reference-descriptor, an exception will
        // be thrown here.
        BusinessObjectRelationship boRel = businessObjectMetaDataService.getBusinessObjectRelationship( bo, referenceName );
        Map<String,String> fkMap = null;
        if ( boRel != null ) {
        	fkMap = boRel.getParentToChildReferences();
        } else {
        	fkMap = Collections.EMPTY_MAP;
        }

        // walk through the foreign keys, testing each one to see if it has a value
        Map pkMap = new HashMap();
        for (Iterator iter = fkMap.keySet().iterator(); iter.hasNext();) {
            String fkFieldName = (String) iter.next();
            String pkFieldName = (String) fkMap.get(fkFieldName);

            // attempt to retrieve the value for the given field
            Object fkFieldValue;
            try {
                fkFieldValue = PropertyUtils.getProperty(bo, fkFieldName);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            // determine if there is a value for the field
            if (ObjectUtils.isNull(fkFieldValue)) {
                allFkeysHaveValues = false;
                break; // no reason to continue processing the fkeys
            }
            else if (String.class.isAssignableFrom(fkFieldValue.getClass())) {
                if (StringUtils.isEmpty((String) fkFieldValue)) {
                    allFkeysHaveValues = false;
                    break;
                }
                else {
                    pkMap.put(pkFieldName, fkFieldValue);
                }
            }

            // if there is a value, grab it
            else {
                pkMap.put(pkFieldName, fkFieldValue);
            }
        }

        // only do the retrieval if all Foreign Keys have values
        if (allFkeysHaveValues) {
        	if (ExternalizableBusinessObject.class.isAssignableFrom(referenceClass)) {
        		ModuleService responsibleModuleService = KNSServiceLocator.getKualiModuleService().getResponsibleModuleService(referenceClass);
				if(responsibleModuleService!=null) {
					return (BusinessObject)responsibleModuleService.getExternalizableBusinessObject(referenceClass, pkMap);
				}
        	} else
        		referenceBo = findByPrimaryKey(referenceClass, pkMap);
        }

        // return what we have, it'll be null if it was never retrieved
        return referenceBo;
    }

    /**
     * 
     * @see org.kuali.rice.kns.service.BusinessObjectService#linkUserFields(org.kuali.rice.kns.bo.BusinessObject)
     */
    public void linkUserFields(PersistableBusinessObject bo) {
        if (bo == null) {
            throw new IllegalArgumentException("bo passed in was null");
        }

        bo.linkEditableUserFields();
       
        linkUserFields( Collections.singletonList( bo ) );
    }

    /**
     * 
     * @see org.kuali.rice.kns.service.BusinessObjectService#linkUserFields(java.util.List)
     */
    public void linkUserFields(List<PersistableBusinessObject> bos) {

        // do nothing if there's nothing to process
        if (bos == null) {
            throw new IllegalArgumentException("List of bos passed in was null");
        }
        else if (bos.isEmpty()) {
            return;
        }

        Person person = null;

        for (PersistableBusinessObject bo : bos) {
            // get a list of the reference objects on the BO
            List<BusinessObjectRelationship> relationships = businessObjectMetaDataService.getBusinessObjectRelationships( bo );
            for ( BusinessObjectRelationship rel : relationships ) {
                if ( Person.class.isAssignableFrom( rel.getRelatedClass() ) ) {
                    person = (Person) ObjectUtils.getPropertyValue(bo, rel.getParentAttributeName() );
                    if (person != null) {
                        // find the universal user ID relationship and link the field
                        for ( Map.Entry<String,String> entry : rel.getParentToChildReferences().entrySet() ) {
                            if ( entry.getValue().equals( "principalId" ) ) {
                                linkUserReference(bo, person, rel.getParentAttributeName(), entry.getKey() );
                                break;
                            }
                        }
                    }                    
                }
            }
            if ( persistenceStructureService.isPersistable(bo.getClass())) {
	            Map<String, Class> references = persistenceStructureService.listReferenceObjectFields(bo);
	
	            // walk through the ref objects, only doing work if they are Person objects
	            for ( String refField : references.keySet() ) {
	                Class<?> refClass = references.get(refField);
	                if (Person.class.isAssignableFrom(refClass)) {
	                    person = (Person) ObjectUtils.getPropertyValue(bo, refField);
	                    if (person != null) {
	                        String fkFieldName = persistenceStructureService.getForeignKeyFieldName(bo.getClass(), refField, "principalId");
	                        linkUserReference(bo, person, refField, fkFieldName);
	                    }
	                }
	            }
            }
        }
    }

    /**
     * 
     * This method links a single UniveralUser back to the parent BO based on the authoritative principalName.
     * 
     * @param bo
     * @param referenceFieldName
     * @param referenceClass
     */
    private void linkUserReference(PersistableBusinessObject bo, Person user, String refFieldName, String fkFieldName) {

        // if the UserId field is blank, there's nothing we can do, so quit
        if (StringUtils.isBlank(user.getPrincipalName())) {
            return;
        }

        // attempt to load the user from the user-name, exit quietly if the user isnt found
        Person userFromService = getPersonService().getPersonByPrincipalName(user.getPrincipalName());
        if (userFromService == null) {
            return;
        }

        // attempt to set the universalId on the parent BO
        setBoField(bo, fkFieldName, userFromService.getPrincipalId());
    }

    private void setBoField(PersistableBusinessObject bo, String fieldName, Object fieldValue) {
        try {
            ObjectUtils.setObjectProperty(bo, fieldName, fieldValue.getClass(), fieldValue);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not set field [" + fieldName + "] on BO to value: " + fieldValue.toString() + " (see nested exception for details).", e);
        }
    }

    /**
     * Gets the businessObjectDao attribute.
     * 
     * @return Returns the businessObjectDao.
     */
    protected BusinessObjectDao getBusinessObjectDao() {
        return businessObjectDao;
    }

    /**
     * Sets the businessObjectDao attribute value.
     * 
     * @param businessObjectDao The businessObjectDao to set.
     */
    public void setBusinessObjectDao(BusinessObjectDao businessObjectDao) {
        this.businessObjectDao = businessObjectDao;
    }

    /**
     * Sets the persistenceStructureService attribute value.
     * 
     * @param persistenceStructureService The persistenceStructureService to set.
     */
    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    /**
     * Sets the kualiUserService attribute value.
     */
    @SuppressWarnings("unchecked")
	public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @SuppressWarnings("unchecked")
	protected PersonService getPersonService() {
        return personService != null ? personService : (personService = KIMServiceLocator.getPersonService());
    }

    /**
     * Sets the persistenceService attribute value.
     * 
     * @param persistenceService The persistenceService to set.
     */
    public final void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    protected BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        return businessObjectMetaDataService;
    }

    public void setBusinessObjectMetaDataService(BusinessObjectMetaDataService boMetadataService) {
        this.businessObjectMetaDataService = boMetadataService;
    }

}
