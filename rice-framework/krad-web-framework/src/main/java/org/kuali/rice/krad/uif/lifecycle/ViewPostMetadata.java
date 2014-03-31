/**
 * Copyright 2005-2013 The Kuali Foundation
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
package org.kuali.rice.krad.uif.lifecycle;

import org.kuali.rice.krad.uif.component.Component;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds data about the rendered view that might be needed to handle a post request.
 *
 * <p>When an action is requested on a view (for example add/delete line, field query, so on), it might be
 * necessary to read configuration from the view that was rendered to cary out the action. However, the rendered
 * view is not stored, and the new view is not rendered until after the controller completes. Therefore it is
 * necessary to provide this mechanism.</p>
 *
 * <p>The post metadata is retrieved in the controller though the {@link org.kuali.rice.krad.web.form.UifFormBase}
 * instance</p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class ViewPostMetadata implements Serializable {
    private static final long serialVersionUID = -515221881981451818L;

    private String id;

    private Map<String, ComponentPostMetadata> componentPostMetadataMap;

    private Map<String, PropertyEditor> fieldPropertyEditors;
    private Map<String, PropertyEditor> secureFieldPropertyEditors;

    private Set<String> inputFieldIds;
    private Set<String> allRenderedPropertyPaths;
    private Map<String, List<Object>> addedCollectionObjects;

    /**
     * Default contructor.
     */
    public ViewPostMetadata() {
        fieldPropertyEditors = new HashMap<String, PropertyEditor>();
        secureFieldPropertyEditors = new HashMap<String, PropertyEditor>();
        inputFieldIds = new HashSet<String>();
        addedCollectionObjects = new HashMap<String, List<Object>>();
    }

    /**
     * Constructor that takes the view id.
     *
     * @param id id for the view
     */
    public ViewPostMetadata(String id) {
        this();

        this.id = id;
    }

    /**
     * Id for the view the post metadata is associated with.
     *
     * @return view id
     */
    public String getId() {
        return id;
    }

    /**
     * @see ViewPostMetadata#getId()
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Map containing post metadata for a component keyed by the component id.
     *
     * @return post metadata map, key is component id and value is post metadata
     */
    public Map<String, ComponentPostMetadata> getComponentPostMetadataMap() {
        return componentPostMetadataMap;
    }

    /**
     * @see ViewPostMetadata#getComponentPostMetadataMap()
     */
    public void setComponentPostMetadataMap(Map<String, ComponentPostMetadata> componentPostMetadataMap) {
        this.componentPostMetadataMap = componentPostMetadataMap;
    }

    /**
     * Gets the component post metadata for the given component id.
     *
     * @param componentId id for the component whose post metadata should be retrieved
     * @return post metadata object
     */
    public ComponentPostMetadata getComponentPostMetadata(String componentId) {
        ComponentPostMetadata componentPostMetadata = null;

        if (componentPostMetadataMap != null && (componentPostMetadataMap.containsKey(componentId))) {
            componentPostMetadata = componentPostMetadataMap.get(componentId);
        }

        return componentPostMetadata;
    }

    /**
     * Adds post data for the given component (this is a convenience method for add component post metadata).
     *
     * @param component component instance the data should be added for
     * @param key key for the post data, this will be used to retrieve the value
     * @param value value for the post data
     */
    public void addComponentPostData(Component component, String key, Object value) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null for adding post data");
        }

        addComponentPostData(component.getId(), key, value);
    }

    /**
     * Adds post data for the given component id (this is a convenience method for add component post metadata).
     *
     * @param componentId id for the component the data should be added for
     * @param key key for the post data, this will be used to retrieve the value
     * @param value value for the post data
     */
    public void addComponentPostData(String componentId, String key, Object value) {
        if (value == null) {
            return;
        }

        ComponentPostMetadata componentPostMetadata = initializeComponentPostMetadata(componentId);

        componentPostMetadata.addData(key, value);
    }

    /**
     * Retrieves post data that has been stored for the given component id and key.
     *
     * @param componentId id for the component the data should be retrieved for
     * @param key key for the post data to retrieve
     * @return value for the data, or null if the data does not exist
     */
    public Object getComponentPostData(String componentId, String key) {
        ComponentPostMetadata componentPostMetadata = getComponentPostMetadata(componentId);

        if (componentPostMetadata != null) {
            return componentPostMetadata.getData(key);
        }

        return null;
    }

    /**
     * Initializes a component post metadata instance for the given component.
     *
     * @param component component instance to initialize post metadata for
     * @return post metadata instance
     */
    public ComponentPostMetadata initializeComponentPostMetadata(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null to initialize post metadata");
        }

        return initializeComponentPostMetadata(component.getId());
    }

    /**
     * Initializes a component post metadata instance for the given component id.
     *
     * @param componentId id for the component to initialize post metadata for
     * @return post metadata instance
     */
    public ComponentPostMetadata initializeComponentPostMetadata(String componentId) {
        ComponentPostMetadata componentPostMetadata;

        if (componentPostMetadataMap != null && (componentPostMetadataMap.containsKey(componentId))) {
            componentPostMetadata = componentPostMetadataMap.get(componentId);
        } else {
            componentPostMetadata = new ComponentPostMetadata(componentId);

            if (componentPostMetadataMap == null) {
                componentPostMetadataMap = new HashMap<String, ComponentPostMetadata>();
            }

            componentPostMetadataMap.put(componentId, componentPostMetadata);
        }

        return componentPostMetadata;
    }

    /**
     * Maintains configuration of properties that have been configured for the view (if render was
     * set to true) and there corresponding PropertyEdtior (if configured).
     *
     * <p>Information is pulled out of the View during the lifecycle so it can be used when a form post
     * is done from the View. Note if a field is secure, it will be placed in the
     * {@link #getSecureFieldPropertyEditors()} map instead</p>
     *
     * @return map of property path (full) to PropertyEditor
     */
    public Map<String, PropertyEditor> getFieldPropertyEditors() {
        return fieldPropertyEditors;
    }

    /**
     * Associates a property editor instance with the given property path.
     *
     * @param propertyPath path for the property the editor should be associated with
     * @param propertyEditor editor instance to use when binding data for the property
     */
    public void addFieldPropertyEditor(String propertyPath, PropertyEditor propertyEditor) {
        if (fieldPropertyEditors == null) {
            fieldPropertyEditors = new HashMap<String, PropertyEditor>();
        }

        fieldPropertyEditors.put(propertyPath, propertyEditor);
    }

    /**
     * Maintains configuration of secure properties that have been configured for the view (if
     * render was set to true) and there corresponding PropertyEdtior (if configured).
     *
     * <p>Information is pulled out of the View during the lifecycle so it can be used when a form post
     * is done from the View. Note if a field is non-secure, it will be placed in the
     * {@link #getFieldPropertyEditors()} map instead</p>
     *
     * @return map of property path (full) to PropertyEditor
     */
    public Map<String, PropertyEditor> getSecureFieldPropertyEditors() {
        return secureFieldPropertyEditors;
    }

    /**
     * Associates a secure property editor instance with the given property path.
     *
     * @param propertyPath path for the property the editor should be associated with
     * @param propertyEditor secure editor instance to use when binding data for the property
     */
    public void addSecureFieldPropertyEditor(String propertyPath, PropertyEditor propertyEditor) {
        if (secureFieldPropertyEditors == null) {
            secureFieldPropertyEditors = new HashMap<String, PropertyEditor>();
        }

        secureFieldPropertyEditors.put(propertyPath, propertyEditor);
    }

    /**
     * The set of all the ids of InputFields present on the View
     *
     * @return the InputField ids for this View
     */
    public Set<String> getInputFieldIds() {
        return inputFieldIds;
    }

    /**
     * @see ViewPostMetadata#getInputFieldIds()
     */
    public void setInputFieldIds(Set<String> inputFieldIds) {
        this.inputFieldIds = inputFieldIds;
    }

    /**
     * Set of property paths that have been rendered as part of the lifecycle.
     *
     * <p>Note this will include all property paths (of data fields) that were rendered as part of the
     * last full lifecycle and any component refreshes since then. It will not contain all paths of a view
     * (which would include all pages)</p>
     *
     * @return set of property paths as strings
     */
    public Set<String> getAllRenderedPropertyPaths() {
        return allRenderedPropertyPaths;
    }

    /**
     * @see ViewPostMetadata#getAllRenderedPropertyPaths()
     */
    public void setAllRenderedPropertyPaths(Set<String> allRenderedPropertyPaths) {
        this.allRenderedPropertyPaths = allRenderedPropertyPaths;
    }

    /**
     * Adds a property path to the list of rendered property paths.
     *
     * @param propertyPath property path to add
     * @see ViewPostMetadata#getAllRenderedPropertyPaths()
     */
    public void addRenderedPropertyPath(String propertyPath) {
        if (this.allRenderedPropertyPaths == null) {
            this.allRenderedPropertyPaths = new HashSet<String>();
        }

        this.allRenderedPropertyPaths.add(propertyPath);
    }

    /**
     * The collection objects that were added during the current controller call, these will be emptied after
     * the lifecycle process is run.
     *
     * <p>Note: If a list is empty this means that a collection had an addLine call occur and a new line must
     * be initialized for the collection.</p>
     *
     * @return the collection objects that were added during the current controller call if added through a process
     * other than the collection's own addLine call
     * @see org.kuali.rice.krad.uif.container.CollectionGroupBase
     */
    public Map<String, List<Object>> getAddedCollectionObjects() {
        return addedCollectionObjects;
    }

    /**
     * @see ViewPostMetadata#getAddedCollectionObjects()
     */
    public void setAddedCollectionObjects(Map<String, List<Object>> addedCollectionObjects) {
        this.addedCollectionObjects = addedCollectionObjects;
    }
}
