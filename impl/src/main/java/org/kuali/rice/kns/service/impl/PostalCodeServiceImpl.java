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
package org.kuali.rice.kns.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kns.bo.PostalCode;
import org.kuali.rice.kns.service.CountryService;
import org.kuali.rice.kns.service.KualiModuleService;
import org.kuali.rice.kns.service.PostalCodeService;
import org.kuali.rice.kns.util.KNSPropertyConstants;

public class PostalCodeServiceImpl implements PostalCodeService {
    private static Logger LOG = Logger.getLogger(PostalCodeServiceImpl.class);

    private CountryService countryService;
    private KualiModuleService kualiModuleService;

    /**
     * @see org.kuali.kfs.sys.service.PostalCodeService#getByPrimaryId(java.lang.String)
     */
    public PostalCode getByPostalCodeInDefaultCountry(String postalZipCode) {
        String postalCountryCode = countryService.getDefaultCountry().getPostalCountryCode();

        return this.getByPrimaryId(postalCountryCode, postalZipCode);
    }

    /**
     * @see org.kuali.kfs.sys.service.PostalCodeService#getByPrimaryId(java.lang.String, java.lang.String)
     */
    public PostalCode getByPrimaryId(String postalCountryCode, String postalCode) {
        if (StringUtils.isBlank(postalCountryCode) || StringUtils.isBlank(postalCode)) {
            LOG.debug("neither postalCountryCode nor postalCode can be empty String.");
            return null;
        }

        Map<String, Object> postalCodeMap = new HashMap<String, Object>();
        postalCodeMap.put(KNSPropertyConstants.POSTAL_COUNTRY_CODE, postalCountryCode);
        postalCodeMap.put(KNSPropertyConstants.POSTAL_CODE, postalCode);

        return kualiModuleService.getResponsibleModuleService(PostalCode.class).getExternalizableBusinessObject(PostalCode.class, postalCodeMap);
    }

    public PostalCode getByPostalCodeInDefaultCountryIfNecessary(String postalCode, PostalCode existingPostalCode) {
        String postalCountryCode = countryService.getDefaultCountry().getPostalCountryCode();

        return this.getByPrimaryIdIfNecessary(postalCountryCode, postalCode, existingPostalCode);
    }

    public PostalCode getByPrimaryIdIfNecessary(String postalCountryCode, String postalCode, PostalCode existingPostalCode) {
        if (existingPostalCode != null) {
            String existingCountryCode = existingPostalCode.getPostalCountryCode();
            String existingPostalZipCode = existingPostalCode.getPostalCode();
            if (StringUtils.equals(postalCountryCode, existingCountryCode) && StringUtils.equals(postalCode, existingPostalZipCode)) {
                return existingPostalCode;
            }
        }

        return this.getByPrimaryId(postalCountryCode, postalCode);
    }

    /**
     * Sets the countryService attribute value.
     * 
     * @param countryService The countryService to set.
     */
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * Sets the kualiModuleService attribute value.
     * 
     * @param kualiModuleService The kualiModuleService to set.
     */
    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }
}
