/*
 * Copyright 2005-2008 The Kuali Foundation
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.bo.PersistableBusinessObject;

/**
 * This interface defines methods that a BusinessObjectService must provide.
 * 
 * 
 */
public interface BusinessObjectService {

    /**
     * Saves the passed in object via the persistence layer.
     * 
     * This will throw an IllegalArgumentException (runtime exception) if the object passed in is not a descendent of
     * BusinessObject.
     * 
     * @param bo A BusinessObject instance or descendent that you wish to be stored.
     * 
     */
    public void save(PersistableBusinessObject bo);

    /**
     * Saves the businessObjects on the list via the persistence layer.
     * 
     * This will throw an IllegalArgumentException (runtime exception) if any of the objects passed in is not a descendent of
     * BusinessObject.
     * 
     * @param businessObjects A List<PersistableBusinessObject> of objects to persist.
     * 
     */
    public void save(List<? extends PersistableBusinessObject> businessObjects);

    /**
     * Links up any contained objects, and then Saves the passed in object via the persistence layer.
     * 
     * This will throw an IllegalArgumentException (runtime exception) if the object passed in is not a descendent of
     * BusinessObject.
     * 
     * @param bo A BusinessObject instance or descendent that you wish to be stored.
     * 
     */
    public void linkAndSave(PersistableBusinessObject bo);

    /**
     * Links up any contained objects, and Saves the businessObjects on the list via the persistence layer.
     * 
     * This will throw an IllegalArgumentException (runtime exception) if any of the objects passed in is not a descendent of
     * BusinessObject.
     * 
     * @param businessObjects A List<BusinessObject> of objects to persist.
     * 
     */
    public void linkAndSave(List<? extends PersistableBusinessObject> businessObjects);

    /**
     * Retrieves an object instance identified by its primary key. For composite keys, use {@link #findByPrimaryKey(Class, Map)}
     * 
     * @param clazz
     * @param primaryKey
     * @return
     */
    public <T> T findBySinglePrimaryKey(Class<T> clazz, Object primaryKey);
    
    /**
     * Retrieves an object instance identified by its primary keys and values. This can be done by constructing a map where the key
     * to the map entry is the primary key attribute and the value of the entry being the primary key value. For composite keys,
     * pass in each primaryKey attribute and its value as a map entry.
     * 
     * @param clazz
     * @param primaryKeys
     * @return
     */
    public PersistableBusinessObject findByPrimaryKey(Class clazz, Map primaryKeys);

    /**
     * Retrieves an object instance identified by the class of the given object and the object's primary key values.
     * 
     * @param object
     * @return
     */
    public PersistableBusinessObject retrieve(PersistableBusinessObject object);

    /**
     * Retrieves a collection of business objects populated with data, such that each record in the database populates a new object
     * instance. This will only retrieve business objects by class type.
     * 
     * @param clazz
     * @return
     */
    public Collection findAll(Class clazz);

    /**
     * This method retrieves a collection of business objects populated with data, such that each record in the database populates a
     * new object instance. This will retrieve business objects by class type and also by criteria passed in as key-value pairs,
     * specifically attribute name and its expected value.
     * 
     * @param clazz
     * @param fieldValues
     * @return
     */
    public Collection findMatching(Class clazz, Map fieldValues);

    /**
     * This method retrieves a count of the business objects populated with data which match the criteria in the given Map.
     * 
     * @param clazz
     * @param fieldValues
     * @return number of businessObjects of the given class whose fields match the values in the given expected-value Map
     */
    public int countMatching(Class clazz, Map fieldValues);

    /**
     * This method retrieves a count of the business objects populated with data which match both the positive criteria 
     * and the negative criteria in the given Map.
     * 
     * @param clazz
     * @param positiveFieldValues
     * @param negativeFieldValues
     * @return number of businessObjects of the given class whose fields match the values in the given expected-value Maps
     */
    public int countMatching(Class clazz, Map positiveFieldValues, Map negativeFieldValues);
    
    /**
     * This method retrieves a collection of business objects populated with data, such that each record in the database populates a
     * new object instance. This will retrieve business objects by class type and also by criteria passed in as key-value pairs,
     * specifically attribute name and its expected value. Performs an order by on sort field.
     * 
     * @param clazz
     * @param fieldValues
     * @return
     */
    public Collection findMatchingOrderBy(Class clazz, Map fieldValues, String sortField, boolean sortAscending);

    /**
     * Deletes a business object from the database.
     * 
     * @param bo
     */
    public void delete(PersistableBusinessObject bo);

    /**
     * Deletes each business object in the given List.
     * 
     * @param boList
     */
    public void delete(List<? extends PersistableBusinessObject> boList);

    /**
     * Deletes the object(s) matching the given field values
     * 
     * @param clazz
     * @param fieldValues
     */
    public void deleteMatching(Class clazz, Map fieldValues);

    /**
     * 
     * This method attempts to retrieve the reference from a BO if it exists.
     * 
     * @param bo - populated BusinessObject instance that includes the referenceName property
     * @param referenceName - name of the member/property to load
     * @return A populated object from the DB, if it exists
     * 
     */
    public BusinessObject getReferenceIfExists(BusinessObject bo, String referenceName);

    /**
     * 
     * Updates all KualiUser or Person objects contained within this BO, based on the UserID as the authoritative key. The
     * appropriate foreign-key field in the BO itself is also updated.
     * 
     * This allows UserIDs to be entered on forms, and the back-end will link up correctly based on this non-key field.
     * 
     * @param bo The populated BO (or descendent) instance to be linked & updated
     * 
     */
    public void linkUserFields(PersistableBusinessObject bo);

    /**
     * 
     * Updates all KualiUser or Person objects contained within this BO, based on the UserID as the authoritative key. The
     * appropriate foreign-key field in the BO itself is also updated.
     * 
     * This allows UserIDs to be entered on forms, and the back-end will link up correctly based on this non-key field.
     * 
     * @param bos A List of populated BusinessObject (or descendent) instances to be linked & updated.
     */
    public void linkUserFields(List<PersistableBusinessObject> bos);

}

