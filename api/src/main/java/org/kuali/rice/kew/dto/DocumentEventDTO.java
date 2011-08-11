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
package org.kuali.rice.kew.dto;

import java.io.Serializable;

/**
 * Superclass for all Document Events which can be generated by the routing engine.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public abstract class DocumentEventDTO implements Serializable {

    public static final String ROUTE_LEVEL_CHANGE = "rt_lvl_change";
    public static final String ROUTE_STATUS_CHANGE = "rt_status_change";
    public static final String DELETE_CHANGE = "delete_document";
    public static final String ACTION_TAKEN = "action_taken";
    public static final String BEFORE_PROCESS = "before_process";
    public static final String AFTER_PROCESS = "after_process";
    public static final String LOCK_DOCUMENTS = "lock_documents";

    private String documentEventCode;
    private Long routeHeaderId;
    private String appDocId;
    
    public DocumentEventDTO() {}

    public DocumentEventDTO(String documentEventCode) {
        this.documentEventCode = documentEventCode;
    }
    
    public String getAppDocId() {
        return appDocId;
    }

    public void setAppDocId(String appDocId) {
        this.appDocId = appDocId;
    }

    public String getDocumentEventCode() {
        return documentEventCode;
    }

    public void setDocumentEventCode(String documentEventCode) {
        this.documentEventCode = documentEventCode;
    }

    public Long getRouteHeaderId() {
        return routeHeaderId;
    }

    public void setRouteHeaderId(Long routeHeaderId) {
        this.routeHeaderId = routeHeaderId;
    }
    
}
