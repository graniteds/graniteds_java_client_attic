package org.granite.tide.data.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.granite.tide.PropertyHolder;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;
import org.granite.tide.data.spi.DataManager;
import org.granite.tide.data.spi.EntityDescriptor;


public class ObjectUtil {

    public static boolean isSimple(Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number || value instanceof Date;
    }
    
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

    public static Object copy(Object obj) {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream(500);
            ObjectOutputStream oos = new ObjectOutputStream(buf);
            oos.writeObject(obj);
            ByteArrayInputStream bis = new ByteArrayInputStream(buf.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not copy object " + obj, e);
        }
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
                EntityDescriptor edesc = EntityDescriptor.getEntityDescriptor(obj1);
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
