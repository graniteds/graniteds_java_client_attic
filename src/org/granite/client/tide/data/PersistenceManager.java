package org.granite.client.tide.data;

import java.util.WeakHashMap;


public class PersistenceManager {
    
    private static WeakHashMap<Object, EntityManager> entityManagersByEntity = new WeakHashMap<Object, EntityManager>(1000);
    
    public static EntityManager getEntityManager(Object object) {
        return entityManagersByEntity.get(object);
    }
    
    public static void setEntityManager(Object object, EntityManager entityManager) {
    	if (entityManager == null)
            entityManagersByEntity.remove(object);
    	else
    		entityManagersByEntity.put(object, entityManager);
    }

}
