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
package org.kuali.rice.core.resourceloader;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.config.ConfigurationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A simple {@link ResourceLoader} which wraps a Spring {@link ConfigurableApplicationContext}.
 *
 * Starts and stops the {@link ConfigurableApplicationContext}.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SpringResourceLoader extends BaseResourceLoader {

	private static final Logger LOG = Logger.getLogger(SpringResourceLoader.class);

	private SpringResourceLoader parentSpringResourceLoader;
	
	private ApplicationContext parentContext;
	private ConfigurableApplicationContext context;

	private final String[] fileLocs;

	public SpringResourceLoader(QName name, String fileLoc) {
	    this(name, new String[] { fileLoc });
	}

	public SpringResourceLoader(QName name, String[] fileLocs) {
		super(name);
		this.fileLocs = fileLocs;
	}

	public Object getService(QName serviceName) {
	    	if (!isStarted()) {
	    	    return null;
	    	}
		if (this.getContext().containsBean(serviceName.toString())) {
			Object service = this.getContext().getBean(serviceName.toString());
			return postProcessService(serviceName, service);
		}
		return super.getService(serviceName);
	}

	@Override
	public void start() throws Exception {
		if(!isStarted()){
			LOG.info("Creating Spring context " + StringUtils.join(this.fileLocs, ","));
			if (parentSpringResourceLoader != null && parentContext != null) {
				throw new ConfigurationException("Both a parentSpringResourceLoader and parentContext were defined.  Only one can be defined!");
			}
			if (parentSpringResourceLoader != null) {
				parentContext = parentSpringResourceLoader.getContext();
			}
			this.context = new ClassPathXmlApplicationContext(this.fileLocs, parentContext);
			super.start();
		}
	}

	@Override
	public void stop() throws Exception {
		LOG.info("Stopping Spring context " + StringUtils.join(this.fileLocs, ","));
		this.context.close();
		super.stop();
	}

	public ConfigurableApplicationContext getContext() {
		return this.context;
	}
	
	public void setParentContext(ApplicationContext parentContext) {
		this.parentContext = parentContext;
	}

	public void setParentSpringResourceLoader(
			SpringResourceLoader parentSpringResourceLoader) {
		this.parentSpringResourceLoader = parentSpringResourceLoader;
	}
	
	

}
