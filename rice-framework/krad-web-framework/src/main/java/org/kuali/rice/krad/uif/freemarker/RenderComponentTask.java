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
package org.kuali.rice.krad.uif.freemarker;

import java.io.IOException;
import java.util.Collections;

import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecyclePhase;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleTaskBase;

import freemarker.core.Environment;
import freemarker.core.Macro;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.kuali.rice.krad.uif.view.View;

/**
 * Perform actual rendering on a component during the lifecycle.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class RenderComponentTask extends ViewLifecycleTaskBase<Component> {

    /**
     * Constructor.
     * 
     * @param phase The render phase for the component.
     */
    public RenderComponentTask(ViewLifecyclePhase phase) {
        super(phase, Component.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performLifecycleTask() {
        Component component = (Component) getElementState().getElement();
        LifecycleRenderingContext renderingContext = ViewLifecycle.getRenderingContext();
        renderingContext.clearRenderingBuffer();

        renderingContext.importTemplate(component.getTemplate());

        for (String additionalTemplate : component.getAdditionalTemplates()) {
            renderingContext.importTemplate(additionalTemplate);
        }

        try {
            Environment env = renderingContext.getEnvironment();

            // Check for a single-arg macro, with the parameter name "component"
            // defer for parent rendering if not found
            Macro fmMacro = (Macro) env.getMainNamespace().get(component.getTemplateName());

            if (fmMacro == null) {
                return;
            }

            String[] args = fmMacro.getArgumentNames();
            if (args == null || args.length != 1 || !component.getComponentTypeName().equals(args[0])) {
                return;
            }

            FreeMarkerInlineRenderUtils.renderTemplate(env, component,
                    null, false, false, Collections.<String, TemplateModel> emptyMap());
        } catch (TemplateException e) {
            throw new IllegalStateException("Error rendering component " + component.getId(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Error rendering component " + component.getId(), e);
        }

        component.setRenderedHtmlOutput(renderingContext.getRenderedOutput());
        component.setSelfRendered(true);
    }

}
