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
package org.kuali.rice.kew.edl;

import org.kuali.rice.kew.edl.EDLContext;
import org.kuali.rice.kew.edl.EDLModelComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestEDLModelCompent implements EDLModelComponent {

	protected static boolean isContacted;
	protected static Element configElement;
	
	public void updateDOM(Document dom, Element configElement, EDLContext edlContext) {
		isContacted = true;
		TestEDLModelCompent.configElement = configElement;
	}

	public static boolean isContacted() {
		return isContacted;
	}

	public static void setContacted(boolean isContacted) {
		TestEDLModelCompent.isContacted = isContacted;
	}

	public static Element getConfigElement() {
		return configElement;
	}

	public static void setConfigElement(Element configElement) {
		TestEDLModelCompent.configElement = configElement;
	}

}
