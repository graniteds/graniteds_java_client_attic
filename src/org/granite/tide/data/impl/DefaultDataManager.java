package org.granite.tide.data.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.persistence.LazyableCollection;
import org.granite.tide.collections.ManagedPersistentCollection;
import org.granite.tide.collections.ManagedPersistentMap;
import org.granite.tide.data.Identifiable;


public class DefaultDataManager extends AbstractDataManager {

    @Override
    public void setTrackingHandler(TrackingHandler trackingHandler) {
    }

    @Override
    public Object getProperty(Object object, String propertyName) {
        return null;
    }

    @Override
    public void setProperty(Object object, String propertyName, Object oldValue, Object newValue) {
    }

    @Override
    public void setInternalProperty(Object object, String propertyName, Object value) {
    }

    @Override
    public Map<String, Object> getPropertyValues(Object object, boolean includeReadOnly, boolean includeTransient) {
        return null;
    }

    @Override
    public Map<String, Object> getPropertyValues(Object object, List<String> excludedProperties, boolean includeReadOnly, boolean includeTransient) {
        return null;
    }

    @Override
    public ManagedPersistentCollection<Object> newPersistentCollection(Identifiable parent, String propertyName, LazyableCollection nextList) {
        return null;
    }

    @Override
    public ManagedPersistentMap<Object, Object> newPersistentMap(Identifiable parent, String propertyName, LazyableCollection nextMap) {
        return null;
    }

    @Override
    public void startTracking(Object previous, Object parent) {
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
    }

    @Override
    public void clear() {
    }

    public boolean isDirty() {
        return false;
    }

    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {
    }

    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
    }
    
    @Override
    public void notifyConstraintViolations(Object entity, Set<ConstraintViolation<?>> violations) {    	
    }

}
