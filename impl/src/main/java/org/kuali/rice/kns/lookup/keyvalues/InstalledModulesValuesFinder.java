/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kns.lookup.keyvalues;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.KualiModuleService;
import org.kuali.rice.kns.service.ModuleService;

/**
 * This class returns list of approved document indicator value pairs.
 */
public class InstalledModulesValuesFinder extends KeyValuesBase {

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List keyValues = new ArrayList();
        KualiModuleService kms = KNSServiceLocator.getKualiModuleService();
        for ( ModuleService moduleService : kms.getInstalledModuleServices() ) {
            keyValues.add(new KeyLabelPair(moduleService.getModuleConfiguration().getNamespaceCode(), 
            		moduleService.getModuleConfiguration().getNamespaceCode() + " - " + 
            		kms.getNamespaceName(moduleService.getModuleConfiguration().getNamespaceCode()))); 
        }

        return keyValues;
    }

}
