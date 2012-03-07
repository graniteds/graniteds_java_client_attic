package org.granite.tide.data;

import java.util.List;
import java.util.Map;

import org.granite.persistence.LazyableCollection;
import org.granite.tide.collections.ManagedPersistentCollection;
import org.granite.tide.collections.ManagedPersistentMap;



public interface DataManager {
    
    public void setTrackingHandler(TrackingHandler trackingHandler);

    public boolean isDirty();
    
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
