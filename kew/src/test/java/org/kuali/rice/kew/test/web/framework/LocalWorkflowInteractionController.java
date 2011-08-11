/*
 * Copyright 2005-2008 The Kuali Foundation
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
package org.kuali.rice.kew.test.web.framework;

import javax.servlet.Servlet;

import org.kuali.rice.kew.test.web.WorkflowServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;


/**
 * LocalInteractionController subclass that supplies a WorkflowServletRequest initialized
 * with user session
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LocalWorkflowInteractionController extends LocalInteractionController {
    public LocalWorkflowInteractionController(Servlet servlet) {
        super(servlet);
    }

    protected MockHttpServletRequest createServletRequest(String method, String uri, Script script) {
        WorkflowServletRequest request = new WorkflowServletRequest(method, uri);
        String user = script.getState().getUser();
        if (user != null) {
            request.setUser(user);
            String backdoorid = script.getState().getBackdoorId();
            if (backdoorid != null) {
                request.setBackdoorId(backdoorid);
            }
        }
        return request;
    }
}
