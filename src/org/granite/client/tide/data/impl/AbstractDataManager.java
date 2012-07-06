package org.granite.client.tide.data.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.granite.client.tide.data.Dirty;
import org.granite.client.tide.data.Id;
import org.granite.client.tide.data.Lazy;
import org.granite.client.tide.data.Version;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.EntityDescriptor;
import org.granite.messaging.amf.RemoteClass;
import org.granite.util.Introspector;

public abstract class AbstractDataManager implements DataManager {

    private static Map<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<Class<?>, EntityDescriptor>(50);
    
    
    public EntityDescriptor getEntityDescriptor(Object entity) {
        EntityDescriptor desc = entityDescriptors.get(entity.getClass());
        if (desc != null)
        	return desc;
        	
    	String className;
        if (entity.getClass().isAnnotationPresent(RemoteClass.class))
            className = entity.getClass().getAnnotation(RemoteClass.class).value();
        else
            className = entity.getClass().getName();
        
        String idPropertyName = null, versionPropertyName = null, dirtyPropertyName = null;
        @SuppressWarnings("unused")
		boolean hasDirty = false;
        Field dsField = null, initField = null;
        
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
        
        desc = new EntityDescriptor(className, idPropertyName, versionPropertyName, dirtyPropertyName, dsField, initField, lazy);
        entityDescriptors.put(entity.getClass(), desc);

        return desc;
    }
}
