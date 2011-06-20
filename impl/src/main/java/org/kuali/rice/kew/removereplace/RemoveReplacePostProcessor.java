/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kew.removereplace;

import org.kuali.rice.kew.postprocessor.DefaultPostProcessor;
import org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kew.postprocessor.ProcessDocReport;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.util.KEWConstants;


/**
 * PostProcessor implementation for the Remove/Replace Document.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class RemoveReplacePostProcessor extends DefaultPostProcessor {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RemoveReplacePostProcessor.class);

    @Override
    public ProcessDocReport doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) throws Exception {
	if (KEWConstants.ROUTE_HEADER_PROCESSED_CD.equals(statusChangeEvent.getNewRouteStatus())) {
	    LOG.info("Finalizing RemoveReplaceDocument with ID " + statusChangeEvent.getRouteHeaderId());
	    KEWServiceLocator.getRemoveReplaceDocumentService().finalize(statusChangeEvent.getRouteHeaderId());
	}
	return super.doRouteStatusChange(statusChangeEvent);
    }

}
