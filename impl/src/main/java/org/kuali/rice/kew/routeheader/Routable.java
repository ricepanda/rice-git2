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
package org.kuali.rice.kew.routeheader;

/**
 * Represents a Routable entity in the system.  A Routable has a document ID as well as an indicator
 * as to whether or not this is the current and active instance of the Routable (this is used
 * for versioning).
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public interface Routable {

	public Long getDocumentId();
	public void setDocumentId(Long documentId);
	public Boolean getCurrentInd();
	public void setCurrentInd(Boolean currentInd);
	public Integer getVersionNumber();
	public void setVersionNumber(Integer versionNumber);
	
}
