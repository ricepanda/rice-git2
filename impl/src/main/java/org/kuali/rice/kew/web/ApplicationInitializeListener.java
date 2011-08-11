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
package org.kuali.rice.kew.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.config.Config;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.config.JAXBConfigImpl;
import org.kuali.rice.core.config.SimpleConfig;
import org.kuali.rice.core.util.JSTLConstants;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.ksb.messaging.MessageFetcher;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * A ServletContextListener responsible for boostrapping the core workflow
 * engine lifecycle. The environment Map provided to the CoreLifecycle is
 * derived directly from the ServletContext init parameters.
 */
public class ApplicationInitializeListener implements ServletContextListener {
	private static final long serialVersionUID = -6603009920502691099L;

    private static final Logger LOG = Logger.getLogger(ApplicationInitializeListener.class);

//    private static final String DEFAULT_LOG4J_CONFIG = "kew-default-log4j.properties";

    //private List<Lifecycle> lifeCycles = new LinkedList<Lifecycle>();
    private ConfigurableApplicationContext context = null;

    /**
	 * ServletContextListener interface implementation that schedules the start
	 * of the lifecycle
     */
	public void contextInitialized(ServletContextEvent sce) {
//	    try {
//	    Properties p = new Properties();
//	    p.load(getClass().getClassLoader().getResourceAsStream(DEFAULT_LOG4J_CONFIG));
//	    PropertyConfigurator.configure(p);
//	    } catch (Exception e) {
//		throw new WorkflowRuntimeException(e);
//	    }

		LOG.info("Initializing Workflow...");

        sce.getServletContext().setAttribute("Constants", new JSTLConstants(KEWConstants.class));

        List<String> configLocations = new ArrayList<String>();
		// use the system prop as an override of the default packaged
		// META-INF/workflow.xml
//        String altCoreConfigLocation = System.getProperty(KEWConstants.DEFAULT_CONFIG_LOCATION_PARAM);
//        if (altCoreConfigLocation != null) {
//        	configLocations.add(altCoreConfigLocation);
//        } else {
//        	addDefaultConfigLocation(sce.getServletContext(), configLocations);
//        }
//
//		// use the system property to add additional configurations (useful for
//		// testing)
        String additionalConfigLocations = System.getProperty(KEWConstants.ADDITIONAL_CONFIG_LOCATIONS_PARAM);
        if (!StringUtils.isBlank(additionalConfigLocations)) {
        	String[] additionalConfigLocationArray = additionalConfigLocations.split(",");
        	for (String additionalConfigLocation : additionalConfigLocationArray) {
        		configLocations.add(additionalConfigLocation);
        	}
        }

        String bootstrapSpringBeans = "classpath:org/kuali/rice/kew/config/ServerKEWSpringBeans.xml";
        if (!StringUtils.isBlank(System.getProperty(KEWConstants.BOOTSTRAP_SPRING_FILE))) {
        	bootstrapSpringBeans = System.getProperty(KEWConstants.BOOTSTRAP_SPRING_FILE);
        } else if (!StringUtils.isBlank(sce.getServletContext().getInitParameter(KEWConstants.BOOTSTRAP_SPRING_FILE))) {
            bootstrapSpringBeans = sce.getServletContext().getInitParameter(KEWConstants.BOOTSTRAP_SPRING_FILE);
            LOG.info("Found bootstrap Spring Beans file defined in servlet context: " + bootstrapSpringBeans);
        }
        try {
        	String basePath = findBasePath(sce.getServletContext());
        	Properties baseProps = new Properties();
    		baseProps.putAll(System.getProperties());
    		baseProps.setProperty("workflow.base", basePath);
    		// HACK: need to determine best way to do this...
    		// if the additional config locations property is empty then we need
    		// to explicitly set it so that if we use it in a root config
    		// a value (an empty value) can be found, and the config parser
    		// won't blow up because "additional.config.locations" property
    		// cannot be resolved
    		// An alternative to doing this at the application/module level would
    		// be to push this functionality down into the Rice ConfigFactoryBean
    		// e.g., by writing a simple ResourceFactoryBean that would conditionally
    		// expose the resource, and then plugging the Resource into the ConfigFactoryBean
    		// However, currently, the ConfigFactoryBean operates on String locations, not
    		// Resources.  Spring can coerce string <value>s into Resources, but not vice-versa
    		if (StringUtils.isEmpty(additionalConfigLocations)) {
    		    baseProps.setProperty(KEWConstants.ADDITIONAL_CONFIG_LOCATIONS_PARAM, "");
    		}
    		Config config = new JAXBConfigImpl(baseProps);
    		config.parseConfig();
    		ConfigContext.init(config);

    		context = new ClassPathXmlApplicationContext(bootstrapSpringBeans);
    		context.start();

    		MessageFetcher messageFetcher = new MessageFetcher((Integer)null);
    		KSBServiceLocator.getThreadPool().execute(messageFetcher);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Workflow failed to start properly.  Exiting.", e);
        }
	}

	protected String findBasePath(ServletContext servletContext) {
		String realPath = servletContext.getRealPath("/");
		// if cannot obtain real path (because, e.g., deployed as WAR
		// try a reasonable guess
		if (realPath == null) {
			if (System.getProperty("catalina.base") != null) {
				realPath = System.getProperty("catalina.base");
			} else {
				realPath = ".";
			}
		}
		String basePath = new File(realPath).getAbsolutePath();
        // append a trailing path separator to make relatives paths work in conjunction
        // with empty ("current working directory") basePath
        if (basePath.length() > 0 && !basePath.endsWith(File.separator)) {
            basePath += File.separator;
        }
        return basePath;
	}

	/**
	 * Configures the default config location by first checking the init params
	 * for default locations and then falling back to the standard default
	 * config location.
	 */
	protected void addDefaultConfigLocation(ServletContext context, List<String> configLocations) {
		String defaultConfigLocation = context.getInitParameter(KEWConstants.DEFAULT_CONFIG_LOCATION_PARAM);
		if (!StringUtils.isEmpty(defaultConfigLocation)) {
			String[] locations = defaultConfigLocation.split(",");
			for (String location : locations) {
				configLocations.add(location);
			}
		} else {
			configLocations.add(KEWConstants.DEFAULT_SERVER_CONFIG_LOCATION);
		}
	}

//	public void configureLifeCycles() {
//		lifeCycles.add(new Log4jLifeCycle());
//		String springLocation = ConfigContext.getCurrentContextConfig().getAlternateSpringFile();
//		if (springLocation == null) {
//			springLocation = "ServerSpring.xml";
//		}
//    	lifeCycles.add(new SpringLifeCycle(springLocation));
//    	lifeCycles.add(new WebApplicationGlobalResourceLifecycle());
//    	lifeCycles.add(new ServiceDelegatingLifecycle(KEWServiceLocator.THREAD_POOL));
//    	lifeCycles.add(new ServiceDelegatingLifecycle(KEWServiceLocator.CACHE_ADMINISTRATOR));
//    	lifeCycles.add(new ServiceDelegatingLifecycle(KEWServiceLocator.SERVICE_REGISTRY));
//    	lifeCycles.add(new XmlPipelineLifeCycle());
//    	lifeCycles.add(new EmailReminderLifecycle());
//	}

	public void contextDestroyed(ServletContextEvent sce) {
		LOG.info("Shutting down workflow.");

//        	Lifecycle lifeCycle;
//        	//stop them in the opposite order they were started...
//        	while(! lifeCycles.isEmpty()) {
//        		lifeCycle = (Lifecycle) ((LinkedList)lifeCycles).removeLast();
//        		try {
//        			lifeCycle.stop();
//        		} catch (Exception e) {
//        			LOG.error("Problems shutting down lifecycle " + lifeCycle.getClass().getName(), e);
//        		}
//
//        	}
		try {
			if (context != null) {
				context.close();
			}
		} catch (Exception e) {
			throw new WorkflowRuntimeException("Failed to shutdown workflow.", e);
		}
	}

}
