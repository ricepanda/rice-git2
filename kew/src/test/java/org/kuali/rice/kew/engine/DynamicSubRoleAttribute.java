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
package org.kuali.rice.kew.engine;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.node.NodeState;
import org.kuali.rice.kew.routeheader.DocumentContent;
import org.kuali.rice.kew.rule.AbstractRoleAttribute;
import org.kuali.rice.kew.rule.ResolvedQualifiedRole;
import org.kuali.rice.kew.user.AuthenticationUserId;


public class DynamicSubRoleAttribute extends AbstractRoleAttribute {

    private static final long serialVersionUID = 5147874690575620616L;
	private List roleNames = new ArrayList();
    
    public DynamicSubRoleAttribute() {
        roleNames.add("DynamicSub");
    }

    public List getRoleNames() {
        return roleNames;
    }

    public List getQualifiedRoleNames(String roleName, DocumentContent documentContent) {
        List qualifiedRoleNames = new ArrayList();
        qualifiedRoleNames.add(roleName);
        return qualifiedRoleNames;
    }

    public ResolvedQualifiedRole resolveQualifiedRole(RouteContext routeContext, String roleName, String qualifiedRole) {
        NodeState nodeState = routeContext.getNodeInstance().getNodeState("role");
        if (nodeState == null) {
            return new ResolvedQualifiedRole();
        }
        String networkId = nodeState.getValue();
        String label = "role " + networkId;
        List recipients = new ArrayList();
        recipients.add(new AuthenticationUserId(networkId));
        return new ResolvedQualifiedRole(label, recipients);
    }   
}
