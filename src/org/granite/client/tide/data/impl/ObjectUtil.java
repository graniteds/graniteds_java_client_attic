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

import java.util.Date;

import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.EntityDescriptor;

/**
 * @author William DRAI
 */
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
                    return objectEquals(dataManager, dataManager.getProperty(obj1, edesc.getIdPropertyName()), dataManager.getProperty(obj2, edesc.getIdPropertyName()));
            }
            return ((Identifiable)obj1).getUid().equals(((Identifiable)obj2).getUid());
        }
        
        if (obj1 == null)
        	return obj2 == null;
        
        return obj1.equals(obj2);
    }
}
