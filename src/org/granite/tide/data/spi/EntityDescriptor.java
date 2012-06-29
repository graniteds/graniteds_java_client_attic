package org.granite.tide.data.spi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.granite.logging.Logger;


public class EntityDescriptor {
    
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger("org.granite.tide.data.EntityDescriptor");
    
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
