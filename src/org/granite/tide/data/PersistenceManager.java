package org.granite.tide.data;

import java.util.WeakHashMap;


public class PersistenceManager {
    
    private static WeakHashMap<Object, EntityManager> entityManagersByEntity = new WeakHashMap<Object, EntityManager>(1000);
    
    public static EntityManager getEntityManager(Object object) {
        return entityManagersByEntity.get(object);
    }
    
    public static void setEntityManager(Object object, EntityManager entityManager) {
        entityManagersByEntity.put(object, entityManager);
    }

}
