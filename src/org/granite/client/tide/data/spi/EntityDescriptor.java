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

package org.granite.client.tide.data.spi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class EntityDescriptor {
    
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger("org.granite.client.tide.data.EntityDescriptor");
    
    private final String className;
    private final String idPropertyName;
    private final String versionPropertyName;
    private final String dirtyPropertyName;
    private final Field dsField;
    private final Field initField;
    
    private final Map<String, Boolean> lazy = new HashMap<String, Boolean>();
    
    
    public EntityDescriptor(String className, String idPropertyName, String versionPropertyName, String dirtyPropertyName, Field dsField, Field initField, Map<String, Boolean> lazy) {
    	this.className = className;
    	this.idPropertyName = idPropertyName;
    	this.versionPropertyName = versionPropertyName;
    	this.dirtyPropertyName = dirtyPropertyName;
    	this.dsField = dsField;
        if (dsField != null)
            dsField.setAccessible(true);
    	this.initField = initField;
        if (initField != null)
            initField.setAccessible(true);
   		if (lazy != null)
   			this.lazy.putAll(lazy);
    }
    
    
    public String getClassName() {
        return className;
    }
    
    public String getIdPropertyName() {
        return idPropertyName;
    }
    
    public String getVersionPropertyName() {
        return versionPropertyName;
    }
    
    public String getDirtyPropertyName() {
        return dirtyPropertyName;
    }
    
    public boolean isLazy(String propertyName) {
        return Boolean.TRUE.equals(lazy.get(propertyName));
    }
    
    public void setLazy(String propertyName) {
        lazy.put(propertyName, true);
    }
    
    public Field getDetachedStateField() {
        return dsField;
    }
    
    public Field getInitializedField() {
        return initField;
    }
}
