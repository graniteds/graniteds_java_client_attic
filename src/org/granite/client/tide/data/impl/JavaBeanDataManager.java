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

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentCollection;
import org.granite.client.tide.collections.ManagedPersistentMap;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Transient;
import org.granite.client.tide.data.spi.EntityDescriptor;

/**
 * @author William DRAI
 */
public class JavaBeanDataManager extends AbstractDataManager {

    @Override
    public void setTrackingHandler(TrackingHandler trackingHandler) {
    }

    @Override
    public Object getProperty(Object object, String propertyName) {
        try {
            Method m = object.getClass().getMethod("get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
            return m.invoke(object);
        }
        catch (NoSuchMethodException e) {
            try {
                Method m = object.getClass().getMethod("is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
                return m.invoke(object);
            }
            catch (Exception f) {
                throw new RuntimeException("Could not get property " + propertyName + " on object " + object, f);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get property " + propertyName + " on object " + object, e);
        }
    }

    @Override
    public void setProperty(Object object, String propertyName, Object oldValue, Object newValue) {
        try {
            Method[] methods = object.getClass().getMethods();
            String setter = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            for (Method m : methods) {
                if (m.getName().equals(setter) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isInstance(newValue)) {
                    m.invoke(object, newValue);
                    break;
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get property " + propertyName + " on object " + object, e);
        }
    }

    @Override
    public void setInternalProperty(Object object, String propertyName, Object value) {
    	setProperty(object, propertyName, null, value);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Map<String, Object> getPropertyValues(Object object, boolean includeReadOnly, boolean includeTransient) {
        return getPropertyValues(object, Collections.EMPTY_LIST, includeReadOnly, includeTransient);
    }

    @Override
    public Map<String, Object> getPropertyValues(Object object, List<String> excludedProperties, boolean includeReadOnly, boolean includeTransient) {
        EntityDescriptor desc = getEntityDescriptor(object);
        
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (Method m : object.getClass().getMethods()) {
            if (!m.getName().startsWith("get") && !m.getName().startsWith("is"))
                continue;
            if (m.getDeclaringClass().equals(Object.class))
            	continue;
            
            if (!includeTransient && m.isAnnotationPresent(Transient.class))
                continue;
            
            String pname = Introspector.decapitalize(m.getName().substring(m.getName().startsWith("get") ? 3 : 2));
            
            if (!includeReadOnly) {
            	try {
            		object.getClass().getMethod("set" + pname.substring(0, 1).toUpperCase() + pname.substring(1), m.getReturnType());
            	}
            	catch (NoSuchMethodException e) {
            		continue;
            	}            	
            }            
            
            if (desc.getDirtyPropertyName() != null && desc.getDirtyPropertyName().equals(pname))
                continue;
            
            try {
                if (excludedProperties.contains(pname))
                    continue;
                
                Object value = m.invoke(object);
                
                values.put(pname, value);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get property " + m + " on object " + object, e);
            }
        }
        return values;    
    }

    @Override
    public ManagedPersistentCollection<Object> newPersistentCollection(Identifiable parent, String propertyName, LazyableCollection nextList) {
        return null;
    }

    @Override
    public ManagedPersistentMap<Object, Object> newPersistentMap(Identifiable parent, String propertyName, LazyableCollection nextMap) {
        return null;
    }

    @Override
    public void startTracking(Object previous, Object parent) {
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
    }

    @Override
    public void clear() {
    }

    public boolean isDirty() {
        return false;
    }

    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {    	
    }

    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
    }
    
    @Override
    public void notifyConstraintViolations(Object entity, Set<ConstraintViolation<?>> violations) {    	
    }

}
