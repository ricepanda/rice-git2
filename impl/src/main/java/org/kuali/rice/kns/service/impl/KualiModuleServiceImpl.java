/*
 * Copyright 2006-2007 The Kuali Foundation
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
package org.kuali.rice.kns.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kuali.rice.kns.bo.ExternalizableBusinessObject;
import org.kuali.rice.kns.bo.Namespace;
import org.kuali.rice.kns.exception.KualiException;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.KualiModuleService;
import org.kuali.rice.kns.service.ModuleService;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class KualiModuleServiceImpl implements KualiModuleService, InitializingBean, ApplicationContextAware {

    private List<ModuleService> installedModuleServices = new ArrayList<ModuleService>();;
    private boolean loadRiceInstalledModuleServices;
    private ApplicationContext applicationContext;
    
    /**
	 * @param applicationContext the applicationContext to set
	 */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public List<ModuleService> getInstalledModuleServices() {
        return installedModuleServices;
    }

    public ModuleService getModuleService(String moduleId) {
        for (ModuleService moduleService : installedModuleServices) {
            if ( moduleService.getModuleConfiguration().getNamespaceCode().equals( moduleId ) ) {
                return moduleService;
            }
        } 
        return null;
    }

    
    /**
     * @see org.kuali.rice.kns.service.KualiModuleService#getModuleServiceByCode(java.lang.String)
     */
    public ModuleService getModuleServiceByNamespaceCode(String namespaceCode) {
        for (ModuleService moduleService : installedModuleServices) {
            if ( moduleService.getModuleConfiguration().getNamespaceCode().equals( namespaceCode ) ) {
                return moduleService;
            }
        } 
        return null;
    }

    public boolean isModuleServiceInstalled(String namespaceCode) {
        for (ModuleService moduleService : installedModuleServices) {
            if ( moduleService.getModuleConfiguration().getNamespaceCode().equals( namespaceCode ) ) {
                return true;
            }
        } 
        return false;
    }

    public ModuleService getResponsibleModuleService(Class boClass) {
    	if(boClass==null) return null;
    	for (ModuleService moduleService : installedModuleServices) {
    	    if ( moduleService.isResponsibleFor( boClass ) ) {
    	        return moduleService;
    	    }
    	}
    	//Throwing exception only for externalizable business objects
    	if(ExternalizableBusinessObject.class.isAssignableFrom(boClass)){
    	    String message;
    		if(!boClass.isInterface())
    			message = "There is no responsible module for the externalized business object class: "+boClass;
    		else
    			message = "There is no responsible module for the externalized business object interface: "+boClass;
    		throw new KualiException(message);
    	} 
    	//Returning null for business objects other than externalizable to keep the framework backward compatible
    	return null;
    }

    /***
     * @see org.kuali.core.service.KualiModuleService#getResponsibleModuleServiceForJob(java.lang.String)
     */
    public ModuleService getResponsibleModuleServiceForJob(String jobName){
        for(ModuleService moduleService : installedModuleServices){
            if(moduleService.isResponsibleForJob(jobName)){
                return moduleService;
            }
        }
        return null;
    }
    
    public void setInstalledModuleServices(List<ModuleService> installedModuleServices) {
        this.installedModuleServices = installedModuleServices;
    }

    public List<String> getDataDictionaryPackages() {
        List<String> packages  = new ArrayList<String>();
        for ( ModuleService moduleService : installedModuleServices ) {
            if ( moduleService.getModuleConfiguration().getDataDictionaryPackages() != null ) {
                packages.addAll( moduleService.getModuleConfiguration().getDataDictionaryPackages() );
            }
        }
        return packages;
    }

	/***
     * 
     * This method uses BusinessObjectService to get the namespace name
     * 
     * @see org.kuali.core.service.KualiModuleService#getNamespaceName(java.lang.String)
     */
    public String getNamespaceName(final String namespaceCode){
    	Namespace parameterNamespace = (Namespace) 
			KNSServiceLocator.getBusinessObjectService().findByPrimaryKey(
					Namespace.class, new HashMap() {{put(KNSPropertyConstants.CODE, namespaceCode);}});
    	return parameterNamespace==null ? "" : parameterNamespace.getName();
    }
    
	/**
	 * @param loadRiceInstalledModuleServices the loadRiceInstalledModuleServices to set
	 */
	public void setLoadRiceInstalledModuleServices(
			boolean loadRiceInstalledModuleServices) {
		this.loadRiceInstalledModuleServices = loadRiceInstalledModuleServices;
	}

	/***
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if(loadRiceInstalledModuleServices){
			try {
				installedModuleServices.addAll(
						KNSServiceLocator.getNervousSystemContextBean(KualiModuleService.class).getInstalledModuleServices());
			} catch ( NoSuchBeanDefinitionException ex ) {
				installedModuleServices.addAll( ((KualiModuleService)applicationContext.getBean( KNSServiceLocator.KUALI_MODULE_SERVICE )).getInstalledModuleServices() );
			}
		}
	}

}

