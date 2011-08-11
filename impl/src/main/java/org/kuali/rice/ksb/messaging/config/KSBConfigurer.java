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
package org.kuali.rice.ksb.messaging.config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.config.Config;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.core.config.ConfigurationException;
import org.kuali.rice.core.config.ModuleConfigurer;
import org.kuali.rice.core.config.event.AfterStartEvent;
import org.kuali.rice.core.config.event.RiceConfigEvent;
import org.kuali.rice.core.lifecycle.Lifecycle;
import org.kuali.rice.core.lifecycle.ServiceDelegatingLifecycle;
import org.kuali.rice.core.resourceloader.ResourceLoader;
import org.kuali.rice.core.resourceloader.SpringLoader;
import org.kuali.rice.core.util.ClassLoaderUtils;
import org.kuali.rice.core.util.OrmUtils;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.ksb.messaging.AlternateEndpoint;
import org.kuali.rice.ksb.messaging.AlternateEndpointLocation;
import org.kuali.rice.ksb.messaging.ServiceDefinition;
import org.kuali.rice.ksb.messaging.resourceloader.KSBResourceLoaderFactory;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.kuali.rice.ksb.util.KSBConstants;
import org.quartz.Scheduler;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * Used to configure the embedded workflow. This could be used to configure
 * embedded workflow programmatically but mostly this is a base class by which
 * to hang specific configuration behavior off of through subclassing
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 * 
 */
public class KSBConfigurer extends ModuleConfigurer {

	private List<ServiceDefinition> services = new ArrayList<ServiceDefinition>();
	
    private List<AlternateEndpointLocation> alternateEndpointLocations = new ArrayList<AlternateEndpointLocation>();

	private List<AlternateEndpoint> alternateEndpoints = new ArrayList<AlternateEndpoint>();

	private String serviceServletUrl;

	private String keystoreAlias;

	private String keystorePassword;

	private String keystoreFile;

	private String webservicesUrl;

	private String webserviceRetry;

	private DataSource registryDataSource;

	private DataSource messageDataSource;
	
	private DataSource nonTransactionalMessageDataSource;

	private String registryDataSourceJndiName;

    private String messageDataSourceJndiName;

    private String nonTransactionalMessageDataSourceJndiName;

	private Scheduler exceptionMessagingScheduler;

	private PlatformTransactionManager platformTransactionManager;

	private boolean isStarted = false;

	public KSBConfigurer() {
        super();
        setModuleName( "KSB" );
        setHasWebInterface(true);
        VALID_RUN_MODES.remove(EMBEDDED_RUN_MODE);
        VALID_RUN_MODES.remove( REMOTE_RUN_MODE );
        VALID_RUN_MODES.remove( THIN_RUN_MODE );
    }
	
	public Config loadConfig(Config parentConfig) throws Exception {
		Config currentConfig = super.loadConfig(parentConfig);
		configureDataSource(currentConfig);
		configureBus(currentConfig);
		configureKeystore(currentConfig);
		configureScheduler(currentConfig);
		configurePlatformTransactionManager(currentConfig);
		if (getServiceServletUrl() != null) {
			currentConfig.putProperty("http.service.url", getServiceServletUrl());
		}
		configureAlternateEndpoints(currentConfig);
		return currentConfig;
	}

	@Override
	public String getSpringFileLocations(){
	    String files = "classpath:org/kuali/rice/ksb/config/KSBSpringBeans.xml" + SpringLoader.SPRING_SEPARATOR_CHARACTER;
        
        if (OrmUtils.isJpaEnabled("rice.ksb")) {
            files += "classpath:org/kuali/rice/ksb/config/KSBJPASpringBeans.xml";
        }
        else {
            files += "classpath:org/kuali/rice/ksb/config/KSBOJBSpringBeans.xml";
        }
        
        if (Boolean.valueOf(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.LOAD_KNS_MODULE_CONFIGURATION))) {
        	files += SpringLoader.SPRING_SEPARATOR_CHARACTER + "classpath:org/kuali/rice/ksb/config/KSBModuleConfigurationSpringBeans.xml";
        }
        
        return files;
	}
	
	/**
	 * Returns true - KSB UI should always be included.
	 * 
	 * @see org.kuali.rice.core.config.ModuleConfigurer#shouldRenderWebInterface()
	 */
	@Override
	public boolean shouldRenderWebInterface() {
		return true;
	}
	
	@Override
	public ResourceLoader getResourceLoaderToRegister() throws Exception{
		ResourceLoader ksbRemoteResourceLoader = KSBResourceLoaderFactory.createRootKSBRemoteResourceLoader();
		ksbRemoteResourceLoader.start();
		return ksbRemoteResourceLoader;
	}
	
	@Override
	protected List<Lifecycle> loadLifecycles() throws Exception {
		List<Lifecycle> lifecycles = new LinkedList<Lifecycle>();

		// this validation of our service list needs to happen after we've
		// loaded our configs so it's a lifecycle
		lifecycles.add(new Lifecycle() {
			boolean started = false;

			public boolean isStarted() {
				return this.started;
			}

			public void start() throws Exception {
				// first check if we want to allow self-signed certificates for SSL communication
				if (new Boolean(ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.KSB_ALLOW_SELF_SIGNED_SSL))) {
				    Protocol.registerProtocol("https", new Protocol("https",
					    (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443));
				}
	
				for (final ServiceDefinition serviceDef : KSBConfigurer.this.services) {
					serviceDef.validate();
				}
				this.started = true;
			}

			public void stop() throws Exception {
				this.started = false;
			}
		});
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.THREAD_POOL_SERVICE));
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.SCHEDULED_THREAD_POOL_SERVICE));
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.REPEAT_TOPIC_INVOKING_QUEUE));
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.OBJECT_REMOTER));
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.BUS_ADMIN_SERVICE));
		lifecycles.add(new ServiceDelegatingLifecycle(KSBServiceLocator.REMOTED_SERVICE_REGISTRY));
		return lifecycles;
	}
	
	

	/**
     * Used to refresh the service registry after the Application Context is initialized.  This way any services that were exported on startup
     * will be available in the service registry once startup is complete.
     */
    @Override
    public void onEvent(RiceConfigEvent event) throws Exception {
        if (event instanceof AfterStartEvent) {
            LOG.info("Refreshing Service Registry to export services to the bus.");
            KSBServiceLocator.getServiceDeployer().refresh();
        }
    }

    protected String getServiceNamespace(Config config) {
		if (StringUtils.isBlank(config.getServiceNamespace())) {
			throw new ConfigurationException("The 'service.namespace' property was not properly configured.");
		}
		return config.getServiceNamespace();
	}

	@SuppressWarnings("unchecked")
	protected void configureBus(Config config) throws Exception {
		LOG.debug("Configuring services for Service Namespace " + ConfigContext.getCurrentContextConfig().getServiceNamespace() + " using config for classloader " + ClassLoaderUtils.getDefaultClassLoader());
		configureServiceList(config, Config.BUS_DEPLOYED_SERVICES, getServices());
	}

	@SuppressWarnings("unchecked")
	protected void configureServiceList(Config config, String key, List services) throws Exception {
		LOG.debug("Configuring services for Service Namespace " + ConfigContext.getCurrentContextConfig().getServiceNamespace() + " using config for classloader " + ClassLoaderUtils.getDefaultClassLoader());
		List<ServiceDefinition> serviceDefinitions = (List<ServiceDefinition>) config.getObject(key);
		if (serviceDefinitions == null) {
			config.putObject(key, services);
		} else if (services != null) {
			LOG.debug("Services already exist.  Adding additional services");
			serviceDefinitions.addAll(services);
		}

		// if it's empty, then we want to be able to inherit it from the parent
		// configuration
		if (!StringUtils.isEmpty(this.serviceServletUrl)) {
			config.putObject(Config.SERVICE_SERVLET_URL, this.serviceServletUrl);
			config.putProperty(Config.SERVICE_SERVLET_URL, this.serviceServletUrl);
		}
	}

	protected void configureScheduler(Config config) {
		if (this.getExceptionMessagingScheduler() != null) {
			LOG.info("Configuring injected exception messaging Scheduler");
			config.putObject(KSBConstants.INJECTED_EXCEPTION_MESSAGE_SCHEDULER_KEY, this.getExceptionMessagingScheduler());
		}
	}

	protected void configureKeystore(Config config) {
		if (!StringUtils.isEmpty(this.keystoreAlias)) {
			config.putProperty(Config.KEYSTORE_ALIAS, this.keystoreAlias);
		}
		if (!StringUtils.isEmpty(this.keystorePassword)) {
			config.putProperty(Config.KEYSTORE_PASSWORD, this.keystorePassword);
		}
		if (!StringUtils.isEmpty(this.keystoreFile)) {
			config.putProperty(Config.KEYSTORE_FILE, this.keystoreFile);
		}
	}

	protected void configureDataSource(Config config) {
        if (getMessageDataSource() != null && getRegistryDataSource() == null) {
            throw new ConfigurationException("A message data source was defined but a registry data source was not defined.  Both must be specified.");
        }
        if (getMessageDataSource() == null && getRegistryDataSource() != null) {
            throw new ConfigurationException("A registry data source was defined but a message data source was not defined.  Both must be specified.");
        }

        if (getMessageDataSource() != null) {
            config.putObject(KSBConstants.KSB_MESSAGE_DATASOURCE, getMessageDataSource());
        } else if (!StringUtils.isBlank(getMessageDataSourceJndiName())) {
            config.putProperty(KSBConstants.KSB_MESSAGE_DATASOURCE_JNDI, getMessageDataSourceJndiName());
        }
        if (getNonTransactionalMessageDataSource() != null) {
            config.putObject(KSBConstants.KSB_MESSAGE_NON_TRANSACTIONAL_DATASOURCE, getNonTransactionalMessageDataSource());
        } else if (!StringUtils.isBlank(getMessageDataSourceJndiName())) {
            config.putProperty(KSBConstants.KSB_MESSAGE_NON_TRANSACTIONAL_DATASOURCE_JNDI, getNonTransactionalMessageDataSourceJndiName());
        }
        if (getRegistryDataSource() != null) {
            config.putObject(KSBConstants.KSB_REGISTRY_DATASOURCE, getRegistryDataSource());
        } else if (!StringUtils.isBlank(getRegistryDataSourceJndiName())) {
            config.putProperty(KSBConstants.KSB_REGISTRY_DATASOURCE_JNDI, getRegistryDataSourceJndiName());
        }
    }

	protected void configurePlatformTransactionManager(Config config) {
		if (getPlatformTransactionManager() == null) {
			return;
		}
		config.putObject(RiceConstants.SPRING_TRANSACTION_MANAGER, getPlatformTransactionManager());
	}
	
	protected void configureAlternateEndpoints(Config config) {
		config.putObject(KSBConstants.KSB_ALTERNATE_ENDPOINT_LOCATIONS, getAlternateEndpointLocations());
		config.putObject(KSBConstants.KSB_ALTERNATE_ENDPOINTS, getAlternateEndpoints());
	}
	
	public void stop() throws Exception {
	    super.stop();
	    cleanUpConfiguration();
	}
	
	/**
     * Because our configuration is global, shutting down Rice does not get rid of objects stored there.  For that reason
     * we need to manually clean these up.  This is most important in the case of the service bus because the configuration
     * is used to store services to be exported.  If we don't clean this up then a shutdown/startup within the same
     * class loading context causes the service list to be doubled and results in "multiple endpoint" error messages.
     *
     */
    protected void cleanUpConfiguration() {
        ConfigContext.getCurrentContextConfig().removeObject(Config.BUS_DEPLOYED_SERVICES);
        ConfigContext.getCurrentContextConfig().removeObject(KSBConstants.KSB_ALTERNATE_ENDPOINTS);
    }

	public boolean isStarted() {
		return this.isStarted;
	}

	protected void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	public List<ServiceDefinition> getServices() {
		return this.services;
	}

	public void setServices(List<ServiceDefinition> javaServices) {
		this.services = javaServices;
	}

	public String getKeystoreAlias() {
		return this.keystoreAlias;
	}

	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}

	public String getKeystoreFile() {
		return this.keystoreFile;
	}

	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public String getKeystorePassword() {
		return this.keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getWebserviceRetry() {
		return this.webserviceRetry;
	}

	public void setWebserviceRetry(String webserviceRetry) {
		this.webserviceRetry = webserviceRetry;
	}

	public String getWebservicesUrl() {
		return this.webservicesUrl;
	}

	public void setWebservicesUrl(String webservicesUrl) {
		this.webservicesUrl = webservicesUrl;
	}

	public String getServiceServletUrl() {
		return this.serviceServletUrl;
	}

	public void setServiceServletUrl(String serviceServletUrl) {
		if (!StringUtils.isEmpty(serviceServletUrl) && !serviceServletUrl.endsWith("/")) {
			serviceServletUrl += "/";
		}
		this.serviceServletUrl = serviceServletUrl;
	}

	public DataSource getMessageDataSource() {
		return this.messageDataSource;
	}

	public void setMessageDataSource(DataSource messageDataSource) {
		this.messageDataSource = messageDataSource;
	}

    public DataSource getNonTransactionalMessageDataSource() {
        return this.nonTransactionalMessageDataSource;
    }

    public void setNonTransactionalMessageDataSource(DataSource nonTransactionalMessageDataSource) {
        this.nonTransactionalMessageDataSource = nonTransactionalMessageDataSource;
    }

    public String getMessageDataSourceJndiName() {
		return this.messageDataSourceJndiName;
	}

	public void setMessageDataSourceJndiName(String messageDataSourceJndiName) {
		this.messageDataSourceJndiName = messageDataSourceJndiName;
	}

    public String getNonTransactionalMessageDataSourceJndiName() {
        return this.nonTransactionalMessageDataSourceJndiName;
    }

    public void setNonTransactionalMessageDataSourceJndiName(String nonTransactionalMessageDataSourceJndiName) {
        this.nonTransactionalMessageDataSourceJndiName = nonTransactionalMessageDataSourceJndiName;
    }

    public DataSource getRegistryDataSource() {
		return this.registryDataSource;
	}

	public void setRegistryDataSource(DataSource registryDataSource) {
		this.registryDataSource = registryDataSource;
	}

	public String getRegistryDataSourceJndiName() {
		return this.registryDataSourceJndiName;
	}

	public void setRegistryDataSourceJndiName(String registryDataSourceJndiName) {
		this.registryDataSourceJndiName = registryDataSourceJndiName;
	}

	public Scheduler getExceptionMessagingScheduler() {
		return this.exceptionMessagingScheduler;
	}

	public void setExceptionMessagingScheduler(Scheduler exceptionMessagingScheduler) {
		this.exceptionMessagingScheduler = exceptionMessagingScheduler;
	}

	public PlatformTransactionManager getPlatformTransactionManager() {
		return platformTransactionManager;
	}

	public void setPlatformTransactionManager(PlatformTransactionManager springTransactionManager) {
		this.platformTransactionManager = springTransactionManager;
	}

    public List<AlternateEndpointLocation> getAlternateEndpointLocations() {
	return this.alternateEndpointLocations;
    }

    public void setAlternateEndpointLocations(List<AlternateEndpointLocation> alternateEndpointLocations) {
	this.alternateEndpointLocations = alternateEndpointLocations;
	}

    public List<AlternateEndpoint> getAlternateEndpoints() {
        return this.alternateEndpoints;
    }

    public void setAlternateEndpoints(List<AlternateEndpoint> alternateEndpoints) {
        this.alternateEndpoints = alternateEndpoints;
    }
    
}
