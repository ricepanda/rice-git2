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

import java.util.Deque;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.uif.freemarker.LifecycleRenderingContext;
import org.kuali.rice.krad.uif.view.DefaultExpressionEvaluator;
import org.kuali.rice.krad.uif.view.ExpressionEvaluator;
import org.kuali.rice.krad.uif.view.ExpressionEvaluatorFactory;

/**
 * Single-threaded view lifecycle processor implementation.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SynchronousViewLifecycleProcessor extends ViewLifecycleProcessorBase {
    private static final Logger LOG = Logger.getLogger(SynchronousViewLifecycleProcessor.class);

    // pending lifecycle phases.
    private final Deque<ViewLifecyclePhase> pendingPhases = new LinkedList<ViewLifecyclePhase>();

    // the phase currently active on this lifecycle.
    private ViewLifecyclePhase activePhase;

    // the rendering context.
    private LifecycleRenderingContext renderingContext;

    // the expression evaluator to use with this lifecycle.
    private final ExpressionEvaluator expressionEvaluator;

    /**
     * Creates a new synchronous processor for a lifecycle.
     *
     * @param lifecycle The lifecycle to process.
     */
    public SynchronousViewLifecycleProcessor(ViewLifecycle lifecycle) {
        super(lifecycle);

        // The null conditions noted here should not happen in full configured environments
        // Conditional fallback support is in place primary for unit testing.
        ExpressionEvaluatorFactory expressionEvaluatorFactory;
        if (lifecycle.helper == null) {
            LOG.warn("No helper is defined for the view lifecycle, using global expression evaluation factory");
            expressionEvaluatorFactory = KRADServiceLocatorWeb.getExpressionEvaluatorFactory();
        } else {
            expressionEvaluatorFactory = lifecycle.helper.getExpressionEvaluatorFactory();
        }

        if (expressionEvaluatorFactory == null) {
            LOG.warn("No global expression evaluation factory is defined, using DefaultExpressionEvaluator");
            expressionEvaluator = new DefaultExpressionEvaluator();
        } else {
            expressionEvaluator = expressionEvaluatorFactory.createExpressionEvaluator();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void offerPendingPhase(ViewLifecyclePhase pendingPhase) {
        pendingPhases.offer(pendingPhase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushPendingPhase(ViewLifecyclePhase phase) {
        pendingPhases.push(phase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performPhase(ViewLifecyclePhase initialPhase) {
        offerPendingPhase(initialPhase);
        while (!pendingPhases.isEmpty()) {
            ViewLifecyclePhase pendingPhase = pendingPhases.poll();
            pendingPhase.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewLifecyclePhase getActivePhase() {
        return activePhase;
    }

    /**
     * {@inheritDoc}
     */
    public LifecycleRenderingContext getRenderingContext() {
        if (renderingContext == null && ViewLifecycle.isRenderInLifecycle()) {
            ViewLifecycle lifecycle = getLifecycle();
//            this.renderingContext = new LifecycleRenderingContext(lifecycle.model, lifecycle.request,
//                    lifecycle.response);
        }

        return this.renderingContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return this.expressionEvaluator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void setActivePhase(ViewLifecyclePhase phase) {
        if (activePhase != null && phase != null) {
            throw new IllegalStateException("Another phase is already active on this lifecycle thread " + activePhase);
        }

        activePhase = phase;
    }

}
