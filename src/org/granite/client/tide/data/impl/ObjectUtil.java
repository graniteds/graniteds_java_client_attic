package org.granite.client.tide.data.impl;

import java.util.Date;

import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.EntityDescriptor;


public class ObjectUtil {

    public static boolean isSimple(Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number || value instanceof Date;
    }
    
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
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
                EntityDescriptor edesc = dataManager.getEntityDescriptor(obj1);
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
