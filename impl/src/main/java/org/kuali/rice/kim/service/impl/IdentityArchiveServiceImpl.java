/*
 * Copyright 2007-2009 The Kuali Foundation
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
package org.kuali.rice.kim.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kuali.rice.kim.bo.entity.dto.KimEntityDefaultInfo;
import org.kuali.rice.kim.bo.entity.impl.KimEntityDefaultInfoCacheImpl;
import org.kuali.rice.kim.service.IdentityArchiveService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.ksb.service.KSBServiceLocator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This is the default implementation for the IdentityArchiveService. 
 * @see IdentityArchiveService
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
public class IdentityArchiveServiceImpl implements IdentityArchiveService {
	private static final Logger LOG = Logger.getLogger( IdentityArchiveServiceImpl.class );
	
	private BusinessObjectService businessObjectService;

	protected BusinessObjectService getBusinessObjectService() {
		if ( businessObjectService == null ) {
			businessObjectService = KNSServiceLocator.getBusinessObjectService();
		}
		return businessObjectService;
	}

	public KimEntityDefaultInfo getEntityDefaultInfoFromArchive( String entityId ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put(KimConstants.PrimaryKeyConstants.ENTITY_ID, entityId);
    	KimEntityDefaultInfoCacheImpl cachedValue = (KimEntityDefaultInfoCacheImpl)getBusinessObjectService().findByPrimaryKey(KimEntityDefaultInfoCacheImpl.class, criteria);
    	if ( cachedValue == null ) {
    		return null;
    	}
    	return cachedValue.convertCacheToEntityDefaultInfo();
    }

    public KimEntityDefaultInfo getEntityDefaultInfoFromArchiveByPrincipalId( String principalId ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put("principals.principalId", principalId);
    	KimEntityDefaultInfoCacheImpl cachedValue = (KimEntityDefaultInfoCacheImpl)getBusinessObjectService().findByPrimaryKey(KimEntityDefaultInfoCacheImpl.class, criteria);
    	if ( cachedValue == null ) {
    		return null;
    	}
    	return cachedValue.convertCacheToEntityDefaultInfo();
    }

    @SuppressWarnings("unchecked")
	public KimEntityDefaultInfo getEntityDefaultInfoFromArchiveByPrincipalName( String principalName ) {
    	Map<String,String> criteria = new HashMap<String, String>(1);
    	criteria.put("principals.principalName", principalName);
    	Collection<KimEntityDefaultInfoCacheImpl> entities = getBusinessObjectService().findMatching(KimEntityDefaultInfoCacheImpl.class, criteria);
    	if ( entities.isEmpty()  ) {
    		return null;
    	}
    	return entities.iterator().next().convertCacheToEntityDefaultInfo();
    }

    public void saveDefaultInfoToArchive( KimEntityDefaultInfo entity ) {
		KSBServiceLocator.getThreadPool().execute( new SaveEntityDefaultInfoToCacheRunnable( entity ) );
    }
    
	// store the person to the database

	// but do this an alternate thread to prevent transaction issues since this service is non-transactional


	private class SaveEntityDefaultInfoToCacheRunnable implements Runnable {
		private KimEntityDefaultInfo entity;
		/**
		 * 
		 */
		public SaveEntityDefaultInfoToCacheRunnable( KimEntityDefaultInfo entity ) {
			this.entity = entity;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				PlatformTransactionManager transactionManager = KNSServiceLocator.getTransactionManager();
				TransactionTemplate template = new TransactionTemplate(transactionManager);
				template.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						getBusinessObjectService().save( new KimEntityDefaultInfoCacheImpl( entity ) );
						return null;
					}
				});
			} catch (Throwable t) {
				LOG.error("Failed to load transaction manager.", t);
			}
		}
	}

}
