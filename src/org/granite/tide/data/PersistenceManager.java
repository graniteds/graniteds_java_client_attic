package org.granite.tide.data.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.granite.tide.PropertyHolder;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;
import org.granite.tide.data.impl.EntityDescriptor;


public class PersistenceManager {
    
    private static WeakHashMap<Object, EntityManager> entityManagersByEntity = new WeakHashMap<Object, EntityManager>(1000);
    private static Map<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<Class<?>, EntityDescriptor>(50);
    
    public static EntityManager getEntityManager(Object object) {
        return entityManagersByEntity.get(object);
    }
    
    public static void setEntityManager(Object object, EntityManager entityManager) {
        entityManagersByEntity.put(object, entityManager);
    }
    
    public static EntityDescriptor getEntityDescriptor(Object object) {
        EntityDescriptor desc = entityDescriptors.get(object.getClass());
        if (desc == null) {
            desc = new EntityDescriptor(object);
            entityDescriptors.put(object.getClass(), desc);
        }
        return desc;
    }
    
    /**
     *  Equality for objects, using uid property when possible
     *
     *  @param obj1 object
     *  @param obj2 object
     * 
     *  @return true when objects are instances of the same entity
     */ 
    public static boolean objectEquals(DataManager dataManager, Object obj1, Object obj2) {
        if ((obj1 instanceof PropertyHolder && obj2 instanceof Identifiable) || (obj1 instanceof Identifiable && obj2 instanceof PropertyHolder))
            return false;
        
        if (obj1 instanceof Identifiable && obj2 instanceof Identifiable && obj1.getClass() == obj2.getClass()) {
            if (obj1 instanceof Lazyable && (!((Lazyable)obj1).isInitialized() || !((Lazyable)obj2).isInitialized())) {
                // Compare with identifier for uninitialized entities
                EntityDescriptor edesc = PersistenceManager.getEntityDescriptor(obj1);
                if (edesc.getIdPropertyName() != null)
                    return dataManager.getProperty(obj1, edesc.getIdPropertyName()).equals(dataManager.getProperty(obj2, edesc.getIdPropertyName()));
            }
            return ((Identifiable)obj1).getUid().equals(((Identifiable)obj2).getUid());
        }
        
        if (obj1 == null)
        	return obj2 == null;
        
        return obj1.equals(obj2);
    }

}
