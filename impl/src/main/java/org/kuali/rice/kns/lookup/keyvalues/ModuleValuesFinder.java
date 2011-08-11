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
package org.kuali.rice.kns.lookup.keyvalues;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.ModuleService;

public class ModuleValuesFinder extends KeyValuesBase {

    /*
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List keyValues = new ArrayList();
        keyValues.add(new KeyLabelPair("", ""));
        //keyValues.add(getKeyLabelPair(RiceConstants.CROSS_MODULE_CODE, RiceConstants.CROSS_MODULE_NAME));
        for (ModuleService moduleService : KNSServiceLocator.getKualiModuleService().getInstalledModuleServices()) {
            keyValues.add(getKeyLabelPair(moduleService.getModuleConfiguration().getNamespaceCode(), 
            		KNSServiceLocator.getKualiModuleService().getNamespaceName(moduleService.getModuleConfiguration().getNamespaceCode())));
        }
        return keyValues;
    }
    
    private KeyLabelPair getKeyLabelPair(String moduleCode, String moduleName) {
        return new KeyLabelPair(moduleCode, moduleCode + " - " + moduleName);
    }
}
