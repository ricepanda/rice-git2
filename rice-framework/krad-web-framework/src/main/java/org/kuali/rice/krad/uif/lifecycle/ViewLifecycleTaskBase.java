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
package org.kuali.rice.krad.uif.lifecycle;

import org.kuali.rice.krad.uif.util.ProcessLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base abstract implementation for a lifecycle task.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * @param <T> Top level element type for this task
 */
public abstract class ViewLifecycleTaskBase<T> implements ViewLifecycleTask<T> {
    private final Logger LOG = LoggerFactory.getLogger(ViewLifecycleTaskBase.class);

    /**
     * Property value for {@link #getElementType()}.
     */
    private final Class<T> elementType;
    
    /**
     * Property value for {@link #getElementState()}.
     */
    private LifecycleElementState elementState;

    /**
     * Creates a lifecycle processing task for a specific phase.
     * 
     * @param elementState The phase this task is a part of.
     * @param elementType Top level element type.
     */
    protected ViewLifecycleTaskBase(LifecycleElementState elementState, Class<T> elementType) {
        this.elementState = elementState;
        this.elementType = elementType;
    }

    /**
     * Performs phase-specific lifecycle processing tasks.
     */
    protected abstract void performLifecycleTask();

    /**
     * Resets this task to facilitate recycling.
     */
    void recycle() {
        this.elementState = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LifecycleElementState getElementState() {
        return elementState;
    }

    /**
     * Sets the phase on a recycled task.
     * 
     * @param elementState The phase to set.
     * @see #getElementState()
     */
    void setElementState(LifecycleElementState elementState) {
        this.elementState = elementState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getElementType() {
        return this.elementType;
    }

    /**
     * Executes the lifecycle task.
     * 
     * <p>
     * This method performs state validation and updates component view status. Override
     * {@link #performLifecycleTask()} to provide task-specific behavior.
     * </p>
     * 
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        try {
            if (!getElementType().isInstance(elementState.getElement())) {
                return;
            }

            // TODO: REMOVE this restriction
            //            if (ViewLifecycle.getPhase() != elementState) {
            //                throw new IllegalStateException("The phase this task is a part of is not active.");
            //            }

            if (ProcessLogger.isTraceActive()) {
                ProcessLogger.countBegin("lc-task-" + elementState.getViewPhase());
            }

            try {
                performLifecycleTask();
            } finally {

                if (ProcessLogger.isTraceActive()) {
                    ProcessLogger.countEnd("lc-task-" + elementState.getViewPhase(),
                            getClass().getName()
                            + " "
                            + elementState.getClass().getName()
                            + " "
                            + elementState.getElement().getClass().getName()
                            + " "
                            + elementState.getElement().getId());
                }
            }

            // Only recycle successfully processed tasks
            LifecycleTaskFactory.recycle(this);
            
        } catch (Throwable t) {
            LOG.warn("Error in lifecycle phase " + this, t);

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new IllegalStateException("Unexpected error in lifecycle phase " + this, t);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " " + getElementState().getElement().getClass().getSimpleName()
                + " " + getElementState().getElement().getId();
    }

}
