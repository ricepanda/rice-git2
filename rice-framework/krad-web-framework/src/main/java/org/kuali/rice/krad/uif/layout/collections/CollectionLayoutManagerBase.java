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
package org.kuali.rice.krad.uif.layout.collections;

import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.layout.CollectionLayoutManager;
import org.kuali.rice.krad.uif.layout.LayoutManagerBase;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleRestriction;
import org.kuali.rice.krad.uif.widget.Pager;

/**
 * Base class for collection layout managers.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class CollectionLayoutManagerBase extends LayoutManagerBase implements CollectionLayoutManager {
    private static final long serialVersionUID = 5530678364562263669L;

    private Group addLineGroup;
    private Field selectFieldPrototype;
    private FieldGroup subCollectionFieldGroupPrototype;

    private Pager pagerWidget;

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "addLineGroup", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Group getAddLineGroup() {
        return addLineGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddLineGroup(Group addLineGroup) {
        this.addLineGroup = addLineGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "selectFieldPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Field getSelectFieldPrototype() {
        return selectFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectFieldPrototype(Field selectFieldPrototype) {
        this.selectFieldPrototype = selectFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "subCollectionFieldGroupPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public FieldGroup getSubCollectionFieldGroupPrototype() {
        return this.subCollectionFieldGroupPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubCollectionFieldGroupPrototype(FieldGroup subCollectionFieldGroupPrototype) {
        this.subCollectionFieldGroupPrototype = subCollectionFieldGroupPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "pagerWidget", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Pager getPagerWidget() {
        return pagerWidget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPagerWidget(Pager pagerWidget) {
        this.pagerWidget = pagerWidget;
    }
}
