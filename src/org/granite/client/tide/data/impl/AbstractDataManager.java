/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.data.impl;

import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;

import org.granite.client.persistence.Persistence;
import org.granite.client.platform.Platform;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.reflect.Property;
import org.granite.util.UUIDUtil;

/**
 * @author William DRAI
 */
public abstract class AbstractDataManager implements DataManager {
	
	private static final Logger log = Logger.getLogger(AbstractDataManager.class);

    private static ConcurrentMap<Class<?>, Set<String>> lazyPropertiesByClass = new ConcurrentHashMap<Class<?>, Set<String>>(50);
    
    
    protected Persistence persistence = null;
    
    public AbstractDataManager() {
    	initPersistence();
    }
    
    protected void initPersistence() {
    	persistence = Platform.persistence();
    }
    
    public boolean isEntity(Object entity) {
    	return entity != null && persistence.isEntity(entity.getClass());
    }
    
    public Object getId(Object entity) {
    	return persistence.getId(entity);
    }
    
    public boolean hasIdProperty(Object entity) {
    	if (entity == null)
    		return false;
    	return persistence.hasIdProperty(entity.getClass());
    }
    
    public String getDetachedState(Object entity) {
    	return persistence.getDetachedState(entity);
    }
    
    public Object getVersion(Object entity) {
    	return persistence.getVersion(entity);
    }
    
    public boolean hasVersionProperty(Object entity) {
    	if (entity == null)
    		return false;
    	return persistence.hasVersionProperty(entity.getClass());
    }
    
    public String getVersionPropertyName(Object entity) {
    	if (entity == null)
    		return null;
    	Property property = persistence.getVersionProperty(entity.getClass());
    	return property != null ? property.getName() : null;
    }
    
    public Object getPropertyValue(Object entity, String name) {
    	if (entity == null)
    		return null;
    	return persistence.getPropertyValue(entity, name, true);
    }
    
    public void setPropertyValue(Object entity, String name, Object value) {
    	if (entity == null)
    		return;
    	persistence.setPropertyValue(entity, name, value);
    }
    
    public String getUid(Object entity) {
    	if (entity == null)
    		return null;
    	
    	if (persistence.hasUidProperty(entity.getClass())) {
	    	String uid = persistence.getUid(entity);
	    	if (uid == null) {
	    		uid = UUIDUtil.randomUUID();
	    		persistence.setUid(entity, uid);
	    	}
	    	return uid;
    	}
    	Object id = persistence.getId(entity);
    	if (id != null)
    		return entity.getClass().getSimpleName() + ":" + id.toString();
    	return entity.getClass().getSimpleName() + "::" + System.identityHashCode(entity);
    }
    
    public String getCacheKey(Object entity) {
    	if (entity == null)
    		return null;
    	
    	return entity.getClass().getName() + ":" + getUid(entity);
    }
    
    public boolean isInitialized(Object object) {
    	return persistence.isInitialized(object);
    }
    
    
    public void copyUid(Object dest, Object obj) {
        if (isEntity(obj) && persistence.hasUidProperty(obj.getClass()))
        	persistence.setUid(dest, persistence.getUid(obj));
    }
    
    public boolean defineProxy(Object dest, Object obj) {
        if (!isEntity(dest))
            return false;
        
        try {
            if (obj != null) {
                if (persistence.getDetachedState(obj) == null)
                    return false;
                persistence.setId(dest, persistence.getId(obj));
                persistence.setDetachedState(dest, persistence.getDetachedState(obj));
            }
            persistence.setInitialized(dest, false);
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not proxy class " + obj.getClass());
        }
    }
    
    public void copyProxyState(Object dest, Object obj) {
		try {
			persistence.setInitialized(dest, persistence.isInitialized(obj));
			persistence.setDetachedState(dest, persistence.getDetachedState(obj));
		}
	    catch (Exception e) {
	        log.error(e, "Could not copy internal state of object " + ObjectUtil.toString(obj));
	    }
    }
    
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeVersion, boolean includeReadOnly) {
        return persistence.getPropertyValues(entity, true, false, excludeVersion, includeReadOnly);
    }
    
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeIdUid, boolean excludeVersion, boolean includeReadOnly) {
        return persistence.getPropertyValues(entity, true, excludeIdUid, excludeVersion, includeReadOnly);
    }

    
    private Set<String> getLazyPropertyNames(Class<?> entityClass) {
    	Set<String> lazyPropertyNames = lazyPropertiesByClass.get(entityClass);
    	if (lazyPropertyNames == null) {
	    	List<Property> lazyProperties = persistence.getLazyProperties(entityClass);
	    	lazyPropertyNames = new HashSet<String>();
	    	for (Property lazyProperty : lazyProperties)
	    		lazyPropertyNames.add(lazyProperty.getName());
	    	
	    	lazyPropertiesByClass.putIfAbsent(entityClass, lazyPropertyNames);	    	
    	}
    	return lazyPropertyNames;
    }
    
    public boolean isLazyProperty(Object entity, String propertyName) {
		return getLazyPropertyNames(entity.getClass()).contains(propertyName);
    }
    
    public void setLazyProperty(Object entity, String propertyName) {
    	getLazyPropertyNames(entity.getClass()).add(propertyName);    	
    }

    
    public boolean isDirtyEntity(Object entity) {
    	EntityManager entityManager = PersistenceManager.getEntityManager(entity);
    	if (entityManager == null)
    		throw new IllegalStateException("Non managed entity: " + entity);
		return entityManager.isDirtyEntity(entity);
    }
    
    public boolean isDeepDirtyEntity(Object entity) {
    	EntityManager entityManager = PersistenceManager.getEntityManager(entity);
    	if (entityManager == null)
    		throw new IllegalStateException("Non managed entity: " + entity);
		return entityManager.isDeepDirtyEntity(entity);
    }
    
    
    
    private TraversableResolver traversableResolver = new TraversableResolverImpl();
    
    public TraversableResolver getTraversableResolver() {
    	return traversableResolver;
    }
    
    public class TraversableResolverImpl implements TraversableResolver {
    	
    	public boolean isReachable(Object bean, Node propertyPath, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
    		return true;
    	}
    	
    	public boolean isCascadable(Object bean, Node propertyPath, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
    		return true;
    	}

    }
}
