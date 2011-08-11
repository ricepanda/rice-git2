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
package org.kuali.rice.core.lifecycle;

import org.kuali.rice.core.resourceloader.SpringLoader;


/**
 * A Lifecycle which simply initializes the SpringLoader with the correct spring context file.  It's important that
 * we don't actually start the SpringLoader here because that will be handled by the GlobalResourceLoader.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SpringLifeCycle extends BaseLifecycle {

	protected final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SpringLifeCycle.class);
	private String springFileNames;

	public SpringLifeCycle() {
		springFileNames = SpringLoader.DEFAULT_SPRING_FILE;
	}

	public SpringLifeCycle(String springFileName) {
		this.springFileNames = springFileName;
	}

	public void start() throws Exception {
		LOG.warn("Initializing Spring from " + springFileNames);
		try {
			SpringLoader.getInstance().setContextFiles(springFileNames);
			super.start();
		} catch (Exception e) {
			LOG.error("Spring Initialization Failed.", e);
			throw new RuntimeException("Spring Initialization Failed.");
		}
	}

}
