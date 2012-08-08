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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentCollection;
import org.granite.client.tide.collections.ManagedPersistentMap;
import org.granite.client.tide.data.Identifiable;

/**
 * @author William DRAI
 */
public interface DataManager {
    
    public void setTrackingHandler(TrackingHandler trackingHandler);

    public boolean isDirty();
    
    public EntityDescriptor getEntityDescriptor(Object entity);
    
    public Object getProperty(Object object, String propertyName);

    public void setProperty(Object object, String propertyName, Object oldValue, Object newValue);

    public void setInternalProperty(Object object, String propertyName, Object value);
    
    public Map<String, Object> getPropertyValues(Object object, boolean includeReadOnly, boolean includeTransient);
    
    public Map<String, Object> getPropertyValues(Object object, List<String> excludedProperties, boolean includeReadOnly, boolean includeTransient);
    
    public ManagedPersistentCollection<Object> newPersistentCollection(Identifiable parent, String propertyName, LazyableCollection nextList);
    
    public ManagedPersistentMap<Object, Object> newPersistentMap(Identifiable parent, String propertyName, LazyableCollection nextMap);

    
    public static enum TrackingType {        
        COLLECTION,
        MAP,
        ENTITY_PROPERTY,
        ENTITY_COLLECTION,
        ENTITY_MAP
    }
    
    public static enum ChangeKind {
        ADD,
        REMOVE,
        REPLACE,
        UPDATE
    }

    
    /**
     *  Start tracking for specific object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void startTracking(Object previous, Object parent);
    
    /**
     *  Stop tracking for specific object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void stopTracking(Object previous, Object parent);


    public void notifyDirtyChange(boolean oldDirty, boolean dirty);

    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity);
    
    
    public void notifyConstraintViolations(Object entity, Set<ConstraintViolation<?>> violation);
    
    
    /**
     *  Reset all currently tracked objects
     */
    public void clear();          
    
    
    public static interface TrackingHandler {
        
        public void collectionChangeHandler(ChangeKind kind, Object target, int location, Object[] items);
        
        public void mapChangeHandler(ChangeKind kind, Object target, int location, Object[] items);

        public void entityPropertyChangeHandler(Object target, String property, Object oldValue, Object newValue);

        public void entityCollectionChangeHandler(ChangeKind kind, Object target, int location, Object[] items);

        public void entityMapChangeHandler(ChangeKind kind, Object target, int location, Object[] items);
    }


}
