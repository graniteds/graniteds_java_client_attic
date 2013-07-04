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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;

import org.granite.client.persistence.Id;
import org.granite.client.persistence.Lazy;
import org.granite.client.persistence.Persistence;
import org.granite.client.persistence.Version;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.EntityDescriptor;
import org.granite.messaging.amf.RemoteClass;
import org.granite.util.Introspector;

/**
 * @author William DRAI
 */
public abstract class AbstractDataManager implements DataManager {

    private static Map<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<Class<?>, EntityDescriptor>(50);
    
    
    public EntityDescriptor getEntityDescriptor(Object entity) {
    	if (entity == null)
    		throw new IllegalArgumentException("Entity must not be null");
    	
        EntityDescriptor desc = entityDescriptors.get(entity.getClass());
        if (desc != null)
        	return desc;
        	
    	String className;
        if (entity.getClass().isAnnotationPresent(RemoteClass.class))
            className = entity.getClass().getAnnotation(RemoteClass.class).value();
        else
            className = entity.getClass().getName();
        
        String idPropertyName = null, versionPropertyName = null;
        
        Map<String, Boolean> lazy = new HashMap<String, Boolean>();
        for (Method m : entity.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && m.getReturnType() != Void.class) {
                if (m.isAnnotationPresent(Id.class))
                    idPropertyName = Introspector.decapitalize(m.getName().substring(3));
                else if (m.isAnnotationPresent(Version.class))
                    versionPropertyName = Introspector.decapitalize(m.getName().substring(3));
                else if (m.isAnnotationPresent(Lazy.class))
                    lazy.put(Introspector.decapitalize(m.getName().substring(3)), true);
            }
        }
        
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class) && idPropertyName == null) {
                    idPropertyName = f.getName();
                }
                else if (f.isAnnotationPresent(Version.class) && versionPropertyName == null) {
                    versionPropertyName = f.getName();
                }
                else if (f.isAnnotationPresent(Lazy.class))
                    lazy.put(Introspector.decapitalize(f.getName().substring(3)), true);
            }
            clazz = clazz.getSuperclass();
        }
        
        desc = new EntityDescriptor(className, idPropertyName, versionPropertyName, lazy);
        entityDescriptors.put(entity.getClass(), desc);

        return desc;
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
