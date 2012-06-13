package org.granite.tide.data.spi;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;
import org.granite.tide.data.Dirty;
import org.granite.tide.data.Id;
import org.granite.tide.data.Lazy;
import org.granite.tide.data.Version;


public class EntityDescriptor {
    
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger("org.granite.tide.data.EntityDescriptor");
    
    private static Map<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<Class<?>, EntityDescriptor>(50);
    
    private final String className;
    private final String idPropertyName;
    private final String versionPropertyName;
    private final String dirtyPropertyName;
    private final Field dsField;
    private final Field initField;
    
    private final Map<String, Boolean> lazy = new HashMap<String, Boolean>();
    
    
    public EntityDescriptor(Object entity) {        
        if (entity.getClass().isAnnotationPresent(RemoteClass.class))
            className = entity.getClass().getAnnotation(RemoteClass.class).value();
        else
            className = entity.getClass().getName();
        
        String idPropertyName = null, versionPropertyName = null, dirtyPropertyName = null;
        boolean hasDirty = false;
        Field dsField = null, initField = null;
        
        for (Method m : entity.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0 && m.getReturnType() != Void.class) {
                if (m.isAnnotationPresent(Id.class))
                    idPropertyName = Introspector.decapitalize(m.getName().substring(3));
                else if (m.isAnnotationPresent(Version.class))
                    versionPropertyName = Introspector.decapitalize(m.getName().substring(3));
                else if (m.isAnnotationPresent(Lazy.class))
                    lazy.put(Introspector.decapitalize(m.getName().substring(3)), true);
            }
            else if (m.getName().equals("isDirty") && m.getParameterTypes().length == 0 && m.getReturnType() == boolean.class) {
                hasDirty = true;
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
                else if (f.isAnnotationPresent(Dirty.class) && dirtyPropertyName == null) {
                    dirtyPropertyName = f.getName();
                }
                else if (f.getName().equals("__detachedState") && dsField == null)
                    dsField = f;
                else if (f.getName().equals("__initialized") && initField == null)
                    initField = f;
                else if (f.isAnnotationPresent(Lazy.class))
                    lazy.put(Introspector.decapitalize(f.getName().substring(3)), true);
            }
            clazz = clazz.getSuperclass();
        }
        
        this.idPropertyName = idPropertyName;
        this.versionPropertyName = versionPropertyName;
        this.dirtyPropertyName = dirtyPropertyName != null ? dirtyPropertyName : (hasDirty ? "dirty" : null);
        this.dsField = dsField;
        this.initField = initField;
        if (dsField != null)
            dsField.setAccessible(true);
        if (initField != null)
            initField.setAccessible(true);
    }
    
    public static EntityDescriptor getEntityDescriptor(Object object) {
        EntityDescriptor desc = entityDescriptors.get(object.getClass());
        if (desc == null) {
            desc = new EntityDescriptor(object);
            entityDescriptors.put(object.getClass(), desc);
        }
        return desc;
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