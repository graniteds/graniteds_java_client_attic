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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.logging.Logger;
import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentAssociation;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.DataManager.ChangeKind;
import org.granite.client.tide.data.spi.ExpressionEvaluator.Value;
import org.granite.client.tide.data.spi.DirtyCheckContext;
import org.granite.client.tide.data.spi.EntityDescriptor;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.data.spi.Wrapper;
import org.granite.client.tide.server.TrackingContext;
import org.granite.client.util.WeakIdentityHashMap;

/**
 * @author William DRAI
 */
public class DirtyCheckContextImpl implements DirtyCheckContext {
    
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.DirtyCheckContextImpl");
    
    private DataManager dataManager;
    private TrackingContext trackingContext;
    private int dirtyCount = 0;
    private WeakIdentityHashMap<Object, Map<String, Object>> savedProperties = new WeakIdentityHashMap<Object, Map<String, Object>>();
    
    public DirtyCheckContextImpl(DataManager dataManager, TrackingContext trackingContext) {
        this.dataManager = dataManager;
        this.trackingContext = trackingContext;
    }

    @Override
    public void setTrackingContext(TrackingContext trackingContext) {
        this.trackingContext = trackingContext;
    }
    
    public boolean isDirty() {
        return dirtyCount > 0;
    }
    
    public void notifyDirtyChange(boolean oldDirty) {
        if (isDirty() == oldDirty)
            return;
        
        dataManager.notifyDirtyChange(oldDirty, isDirty());
    }
    
    public boolean notifyEntityDirtyChange(Object entity, Object object, boolean oldDirtyEntity) {
        boolean newDirtyEntity = isEntityChanged(object);
        if (newDirtyEntity != oldDirtyEntity)
            dataManager.notifyEntityDirtyChange(entity, oldDirtyEntity, newDirtyEntity);
        return newDirtyEntity;
    }
    
    public Map<String, Object> getSavedProperties(Object entity) {
        return savedProperties.get(entity);
    }
    
    public boolean isSaved(Object entity) {
        return savedProperties.containsKey(entity);
    }
    
    public void clear(boolean notify) {
        boolean wasDirty = isDirty();
        dirtyCount = 0;
        savedProperties.clear();
        if (notify)
            notifyDirtyChange(wasDirty);
    }
    
    /**
     *  Check if entity property has been changed since last remote call
     *
     *  @param entity entity to check
     *  @param propertyName property to check
     *  @param value current value to compare with saved value
     *   
     *  @return true is value has been changed
     */ 
    @SuppressWarnings("unchecked")
	public boolean isEntityChanged(Identifiable entity, String propertyName, Object value) {
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            
            Object source = savedProperties.get(entity);
            Object v;
            if (source == null || !(source instanceof Map<?, ?>))
                v = dataManager.getProperty(entity, propertyName);
            else
                v = ((Map<String, Object>)source).get(propertyName);
            
            return v != value;
        }
        finally {
            trackingContext.setEnabled(saveTracking);
        }
    }
    
    
    public boolean isEntityChanged(Object entity) {
        return isEntityChanged(entity, null, null);
    }
    
    /**
     *  Check if entity has changed since last save point
     *
     *  @param entity entity to check
     *  @param propName property name
     *  @param value
     *   
     *  @return entity is dirty
     */ 
    @SuppressWarnings("unchecked")
	public boolean isEntityChanged(Object entity, String propName, Object value) {
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            
            boolean dirty = false;
            
            Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
            
            EntityDescriptor desc = entity instanceof Identifiable ? dataManager.getEntityDescriptor(entity) : null;
            Map<String, Object> save = savedProperties.get(entity);
            String versionPropertyName = desc != null ? desc.getVersionPropertyName() : null;
            String dirtyPropertyName = desc != null ? desc.getDirtyPropertyName() : null;
            
            for (String p : pval.keySet()) {
                if (p.equals(versionPropertyName) || p.equals(dirtyPropertyName))
                    continue;
                
                Object val = p.equals(propName) ? value : pval.get(p);
                Object saveval = save != null ? save.get(p) : null;
                
                if (save != null && ((val != null && (ObjectUtil.isSimple(val) || val instanceof byte[]))
                        || (saveval != null && (ObjectUtil.isSimple(saveval) || saveval instanceof byte[])))) {
                    dirty = true;
                    break;
                }
                else if (save != null && (val instanceof Value || saveval instanceof Value || val instanceof Enum || saveval instanceof Enum)) {
                    if (saveval != null && ((val == null && saveval != null) || !val.equals(saveval))) {
                        dirty = true;
                        break;
                    }
                }
                else if (save != null && (val instanceof Identifiable || saveval instanceof Identifiable)) {
                    if (saveval != null && val != save.get(p)) {
                        dirty = true;
                        break;
                    }
                }
                else if ((val instanceof List<?> || val instanceof Map<?, ?>) && !(val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())) {
                    List<Change> savedArray = (List<Change>)saveval;
                    if (savedArray != null && !savedArray.isEmpty()) {
                        dirty = true;
                        break;
                    }
                }
                else if (val != null
                    && !(val instanceof Identifiable || val instanceof Enum || val instanceof Value || val instanceof byte[]) 
                    && isEntityChanged(val)) {
                    dirty = true;
                    break;
                }
            }
            return dirty;
        }
        finally {
            trackingContext.setEnabled(saveTracking);
        }
    }


    private boolean isSame(Object val1, Object val2) {
        if (val1 == null && isEmpty(val2))
            return true;
        else if (val2 == null && isEmpty(val1))
            return true;
        else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
            return val1.equals(val2);
        else if (val1 instanceof byte[] && val2 instanceof byte[])
            return Arrays.equals((byte[])val1, (byte[])val2);
        else if ((val1 instanceof Value && val2 instanceof Value) || (val1 instanceof Enum && val2 instanceof Enum))
            return val1.equals(val2);
        
        Object n = val1 instanceof Wrapper ? ((Wrapper)val1).getWrappedObject() : val1;
        Object o = val2 instanceof Wrapper ? ((Wrapper)val2).getWrappedObject() : val2;
        if (n instanceof Identifiable && o instanceof Identifiable)
            return ((Identifiable)n).getUid() != null && ((Identifiable)n).getUid().equals(((Identifiable)o).getUid());
        return n == o;
    }
    
    private boolean isSameExt(Object val1, Object val2) {
        if (val1 == null && isEmpty(val2))
            return true;
        else if (val2 == null && isEmpty(val1))
            return true;
        else if (ObjectUtil.isSimple(val1) && ObjectUtil.isSimple(val2))
            return val1.equals(val2);
        else if (val1 instanceof byte[] && val2 instanceof byte[])
            return Arrays.equals((byte[])val1, (byte[])val2);
        else if ((val1 instanceof Value && val2 instanceof Value) || (val1 instanceof Enum && val2 instanceof Enum))
            return val1.equals(val2);
        else if (val1 != null && val1.getClass().isArray() && val2 != null && val2.getClass().isArray()) {
            if (Array.getLength(val1) != Array.getLength(val2))
                return false;
            for (int idx = 0; idx < Array.getLength(val1); idx++) {
                if (!isSameExt(Array.get(val1, idx), Array.get(val2, idx)))
                    return false;
            }
            return true;
        }
        else if (val1 instanceof Set<?> && val2 instanceof Set<?>) {
			if ((val1 instanceof LazyableCollection && !((LazyableCollection)val1).isInitialized()) 
					|| (val2 instanceof LazyableCollection && !((LazyableCollection)val2).isInitialized()))
				return false;
            Collection<?> coll1 = (Collection<?>)val1;
            Collection<?> coll2 = (Collection<?>)val2;
            if (coll1.size() != coll2.size())
                return false;
            for (Object e : coll1) {
                boolean found = false;
                for (Object f : coll2) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            for (Object e : coll2) {
                boolean found = false;
                for (Object f : coll1) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            return true;
        }
        else if (val1 instanceof List<?> && val2 instanceof List<?>) {
			if ((val1 instanceof LazyableCollection && !((LazyableCollection)val1).isInitialized()) 
					|| (val2 instanceof LazyableCollection && !((LazyableCollection)val2).isInitialized()))
				return false;
            List<?> list1 = (List<?>)val1;
            List<?> list2 = (List<?>)val2;
            if (list1.size() != list2.size())
                return false;
            for (int idx = 0; idx < list1.size(); idx++) {
                if (!isSameExt(list1.get(idx), list2.get(idx)))
                    return false;
            }
            return true;
        }
        else if (val1 instanceof Map<?, ?> && val2 instanceof Map<?, ?>) {
			if ((val1 instanceof LazyableCollection && !((LazyableCollection)val1).isInitialized()) 
					|| (val2 instanceof LazyableCollection && !((LazyableCollection)val2).isInitialized()))
				return false;
            Map<?, ?> map1 = (Map<?, ?>)val1;
            Map<?, ?> map2 = (Map<?, ?>)val2;
            if (map1.size() != map2.size())
                return false;
            for (Object e : map1.keySet()) {
                boolean found = false;
                for (Object f : map2.keySet()) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
                if (!isSameExt(map1.get(e), map2.get(e)))
                    return false;
            }
            for (Object e : map2.keySet()) {
                boolean found = false;
                for (Object f : map1.keySet()) {
                    if (isSameExt(e, f)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
                if (!isSameExt(map1.get(e), map2.get(e)))
                    return false;
            }
            return true;
        }

        Object n = val1 instanceof Wrapper ? ((Wrapper)val1).getWrappedObject() : val1;
        Object o = val2 instanceof Wrapper ? ((Wrapper)val2).getWrappedObject() : val2;
        if (n instanceof Identifiable && o instanceof Identifiable)
            return ((Identifiable)n).getUid().equals(((Identifiable)o).getUid());
        
        return n == o;
    }

    /**
     *  @private 
     *  Interceptor for managed entity setters
     *
     *  @param entity entity to intercept
     *  @param propName property name
     *  @param oldValue old value
     *  @param newValue new value
     */ 
    public void entityPropertyChangeHandler(Object entity, Object object, String propName, Object oldValue, Object newValue) {
        boolean oldDirty = isDirty();
        
        boolean diff = !isSame(oldValue, newValue);
        
        if (diff) {
            boolean oldDirtyEntity = isEntityChanged(object, propName, oldValue);
            
            EntityDescriptor desc = dataManager.getEntityDescriptor(entity);
            Map<String, Object> save = savedProperties.get(object);
            boolean unsaved = save == null;
            
            if (unsaved || (desc.getVersionPropertyName() != null && 
                save.get(desc.getVersionPropertyName()) != dataManager.getProperty(entity, desc.getVersionPropertyName()) 
                    && !(save.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(entity, desc.getVersionPropertyName()) == null))) {
                
                save = new HashMap<String, Object>();
                if (desc.getVersionPropertyName() != null)
                    save.put(desc.getVersionPropertyName(), dataManager.getProperty(entity, desc.getVersionPropertyName()));
                savedProperties.put(object, save);
                save.put(propName, oldValue);
                if (unsaved)
                    dirtyCount++;
            }
            
            if (save != null && (desc.getVersionPropertyName() == null 
                || save.get(desc.getVersionPropertyName()) == dataManager.getProperty(entity, desc.getVersionPropertyName())
                || (save.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(entity, desc.getVersionPropertyName()) == null))) {
                
                if (!save.containsKey(propName))
                    save.put(propName, oldValue);
                
                if (isSame(save.get(propName), newValue)) {
                    save.remove(propName);
                    int count = 0;
                    for (String p : save.keySet()) {
                        if (!p.equals(desc.getVersionPropertyName()))
                            count++;
                    }
                    if (count == 0) {
                        savedProperties.remove(object);
                        dirtyCount--;
                    }
                }
            }
            
            notifyEntityDirtyChange(entity, object, oldDirtyEntity);
        }
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  @private 
     *  Collection event handler to save changes on managed collections
     *
     *  @param owner owner entity of the collection
     *  @param propName property name of the collection
     *  @param event collection event
     */ 
    @SuppressWarnings("unchecked")
	public void entityCollectionChangeHandler(Object owner, String propName, ChangeKind kind, int location, Object[] items) {
        boolean oldDirty = isDirty();
        
        EntityDescriptor desc = dataManager.getEntityDescriptor(owner);
        boolean oldDirtyEntity = isEntityChanged(owner);
        
        Map<String, Object> esave = savedProperties.get(owner);
        boolean unsaved = esave == null;
        
        if (unsaved || (desc.getVersionPropertyName() != null && 
            (esave.get(desc.getVersionPropertyName()) != dataManager.getProperty(owner, desc.getVersionPropertyName()) 
                && !(esave.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(owner, desc.getVersionPropertyName()) == null)))) {

            esave = new HashMap<String, Object>();
            if (desc.getVersionPropertyName() != null)
                esave.put(desc.getVersionPropertyName(), dataManager.getProperty(owner, desc.getVersionPropertyName()));
            savedProperties.put(owner, esave);
            if (unsaved)
                dirtyCount++;
        }
    
        List<Change> save = (List<Change>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Change>();
            esave.put(propName, save);
        }
        
        if (esave != null && (desc.getVersionPropertyName() == null 
            || esave.get(desc.getVersionPropertyName()) == dataManager.getProperty(owner, desc.getVersionPropertyName())
            || (esave.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(owner, desc.getVersionPropertyName()) == null))) {
            
            boolean found = false;
			
			int[] actualLocations = new int[save.size()];
			for (int i = 0; i < save.size(); i++) {
				actualLocations[i] = save.get(i).location;
                for (int j = 0; j < i; j++) {
                    if (save.get(j).kind == ChangeKind.REMOVE && actualLocations[i] <= save.get(j).location)
                        actualLocations[j]--;
					else if (save.get(j).kind == ChangeKind.ADD && actualLocations[i] <= save.get(j).location)
						actualLocations[j]++;
				}
			}

            for (int i = 0; i < save.size(); i++) {                     
                if (((kind == ChangeKind.ADD && save.get(i).getKind() == ChangeKind.REMOVE)
                    || (kind == ChangeKind.REMOVE && save.get(i).getKind() == ChangeKind.ADD))
                    && items.length == 1 && save.get(i).getItems().length == 1 
                    && isSame(items[0], save.get(i).getItems()[0]) && location == actualLocations[i]) {
                    
                    save.remove(i);
                    // Adjust location of other saved events because an element added/removed locally has been removed/added
                    for (int j = i; j < save.size(); j++) {
                    	Change c = save.get(j);
                        if (kind == ChangeKind.REMOVE && c.kind == ChangeKind.ADD && c.location > location)
                            c.moveLocation(-1);
                        else if (kind == ChangeKind.ADD && c.kind == ChangeKind.REMOVE && c.location > location)
                            c.moveLocation(1);
                    }
                    
                    if (save.size() == 0) {
                        esave.remove(propName);
                        int count = 0;
                        for (String p : esave.keySet()) {
                            if (!p.equals(desc.getVersionPropertyName()))
                                count++;
                        }
                        if (count == 0) {
                            savedProperties.remove(owner);
                            dirtyCount--;
                        }
                    }
                    i--;
                    found = true;
                }
                else if (kind == ChangeKind.REPLACE && save.get(i).getKind() == ChangeKind.REPLACE
                    && items.length == 1 && save.get(i).getItems().length == 1
                    && location == actualLocations[i]) {
                    
                    if (isSame(((Object[])items[0])[0], ((Object[])save.get(i).getItems()[0])[1])
                        && isSame(((Object[])items[0])[1], ((Object[])save.get(i).getItems()[0])[0])) {
                        
                        save.remove(i);
                        if (save.isEmpty()) {
                            esave.remove(propName);
                            int count = 0;
                            for (Object p : esave.keySet()) {
                                if (!p.equals(desc.getVersionPropertyName()))
                                    count++;
                            }
                            if (count == 0) {
                                savedProperties.remove(owner);
                                dirtyCount--;
                            }
                        }
                        i--;
                    }
                    else {
                        ((Object[])save.get(i).getItems()[0])[1] = ((Object[])items[0])[1];
                    }
                    found = true;
                }
            }
            if (!found)
                save.add(new Change(kind, location, items));
        }
        
        notifyEntityDirtyChange(owner, owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
    }


        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param owner owner entity of the collection
         *  @param propName property name of the collection
         *  @param event map event
         */ 
    @SuppressWarnings("unchecked")
	public void entityMapChangeHandler(Object owner, String propName, ChangeKind kind, int location, Object[] items) {
        boolean oldDirty = isDirty();
        
        EntityDescriptor desc = dataManager.getEntityDescriptor(owner);
        boolean oldDirtyEntity = isEntityChanged(owner);
        
        Map<String, Object> esave = savedProperties.get(owner);
        boolean unsaved = esave == null;
        
        if (unsaved || (desc.getVersionPropertyName() != null && 
            (esave.get(desc.getVersionPropertyName()) != dataManager.getProperty(owner, desc.getVersionPropertyName()) 
                && !(esave.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(owner, desc.getVersionPropertyName()) == null)))) {

            esave = new HashMap<String, Object>();
            if (desc.getVersionPropertyName() != null)
                esave.put(desc.getVersionPropertyName(), dataManager.getProperty(owner, desc.getVersionPropertyName()));
            savedProperties.put(owner, esave);
            if (unsaved)
                dirtyCount++;
        }
        
        List<Change> save = (List<Change>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Change>();
            esave.put(propName, save);
        }
        
        if (esave != null && (desc.getVersionPropertyName() == null 
            || esave.get(desc.getVersionPropertyName()) == dataManager.getProperty(owner, desc.getVersionPropertyName())
            || (esave.get(desc.getVersionPropertyName()) == null && dataManager.getProperty(owner, desc.getVersionPropertyName()) == null))) {
                
            boolean found = false;

            for (int i = 0; i < save.size(); i++) {
                if (((kind == ChangeKind.ADD && save.get(i).getKind() == ChangeKind.REMOVE)
                    || (kind == ChangeKind.REMOVE && save.get(i).getKind() == ChangeKind.ADD))
                    && items.length == 1 && save.get(i).getItems().length == 1 && location == save.get(i).getLocation() 
                    && isSame(((Object[])items[0])[0], ((Object[])save.get(i).items[0])[0]) && isSame(((Object[])items[0])[1], ((Object[])save.get(i).items[0])[1])) {
                    
                    save.remove(i);
                    if (save.size() == 0) {
                        esave.remove(propName);
                        int count = 0;
                        for (String p : esave.keySet()) {
                            if (!p.equals(desc.getVersionPropertyName()))
                                count++;
                        }
                        if (count == 0) {
                            savedProperties.remove(owner);
                            dirtyCount--;
                        }
                    }
                    i--;
                    found = true;
                }
                else if (kind == ChangeKind.REPLACE && save.get(i).getKind() == ChangeKind.REPLACE
                    && items.length == 1 && save.get(i).getItems().length == 1
                    && isSame(((Object[])items[0])[0], ((Object[])save.get(i).getItems()[0])[0])) {
                    
                    if (isSame(((Object[])items[0])[1], ((Object[])save.get(i).getItems()[0])[2])
                        && isSame(((Object[])items[0])[2], ((Object[])save.get(i).getItems()[0])[1])) {
                        
                        save.remove(i);
                        if (save.isEmpty()) {
                            esave.remove(propName);
                            int count = 0;
                            for (Object p : esave.keySet()) {
                                if (!p.equals(desc.getVersionPropertyName()))
                                    count++;
                            }
                            if (count == 0) {
                                savedProperties.remove(owner);
                                dirtyCount--;
                            }
                        }
                        i--;
                    }
                    else {
                        ((Object[])save.get(i).getItems()[0])[2] = ((Object[])items[0])[2];
                    }
                    found = true;
                }
            }
            if (!found)
                save.add(new Change(kind, location, items));
        }
        
        notifyEntityDirtyChange(owner, owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  @private 
     *  Mark an object merged from the server as not dirty
     *
     *  @param object merged object
     */ 
    public void markNotDirty(Object object, Identifiable entity) {
        if (!savedProperties.containsKey(object))
            return;
        
        boolean oldDirty = isDirty();
        
        boolean oldDirtyEntity = false;
        if (entity == null && object instanceof Identifiable)
            entity = (Identifiable)object;
        if (entity != null)
            oldDirtyEntity = isEntityChanged(entity);
        
        savedProperties.remove(object);
        
        if (entity != null)
            notifyEntityDirtyChange(entity, entity, oldDirtyEntity);
        
        dirtyCount--;

        notifyDirtyChange(oldDirty);
    }
    
    
    /**
     *  @private 
     *  Check if dirty properties of an object are the same than those of another entity
     *  When they are the same, unmark the dirty flag
     *
     *  @param entity merged entity
     *  @param source source entity
     *  @return true if the entity is still dirty after comparing with incoming object
     */ 
    public boolean checkAndMarkNotDirty(Identifiable entity, Identifiable source) {
        Map<String, Object> save = savedProperties.get(entity);
        if (save == null)
            return false;
        
        boolean oldDirty = isDirty();
        boolean oldDirtyEntity = isEntityChanged(entity);
        
        EntityDescriptor desc = dataManager.getEntityDescriptor(entity);
        List<String> merged = new ArrayList<String>();
        
        for (String propName : save.keySet()) {
            if (propName.equals(desc.getVersionPropertyName()))
                continue;
            
            Object localValue = dataManager.getProperty(entity, propName);
            Object sourceValue = dataManager.getProperty(source, propName);
            if (localValue instanceof ManagedPersistentAssociation)
                localValue = ((ManagedPersistentAssociation)localValue).getCollection();
            if (isSameExt(localValue, sourceValue))
                merged.add(propName);
        }
        
        for (String propName : merged)
            save.remove(propName);
        
        int count = 0;
        for (String propName : save.keySet()) {
            if (!propName.equals(desc.getVersionPropertyName()))
                count++;
        }
        if (count == 0) {
            savedProperties.remove(entity);
            dirtyCount--;
        }
        
        boolean newDirtyEntity = notifyEntityDirtyChange(entity, entity, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
        
        return newDirtyEntity;
    }
    
    
    /**
     *  @private
     *  Internal implementation of entity reset
     */ 
    @SuppressWarnings("unchecked")
	public void resetEntity(MergeContext mergeContext, Object entity, Identifiable parent, Set<Object> cache) {
        // Should not try to reset uninitialized entities
        if (entity instanceof Lazyable && !((Lazyable)entity).isInitialized())
            return;
        
        if (cache.contains(entity))
            return;
        cache.add(entity);
        
        Map<String, Object> save = savedProperties.get(entity);
        EntityDescriptor desc = dataManager.getEntityDescriptor(entity);
        
        Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
        
        for (String p : pval.keySet()) {
            if (p.equals(desc.getVersionPropertyName()))
                continue;
            
            List<Object> removed = null;
            
            Object val = dataManager.getProperty(entity, p);
            if (val instanceof List<?> && !(val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())) {
                List<Object> list = (List<Object>)val;
                List<Change> savedArray = save != null ? (List<Change>)save.get(p) : null;
                
                if (savedArray != null) {
                	for (int a = savedArray.size()-1; a >= 0; a--) {
                		if (a >= savedArray.size())	{ // Due to internal changes during remove/add
                			log.debug("WARN: savedArray for collection empty before complete processing %s", list);
                			break;
                		}
                        Change ce = savedArray.get(a);
                        if (ce.getKind() == ChangeKind.ADD) {
                            if (removed == null)
                                removed = new ArrayList<Object>();
                            for (int z = 0; z < ce.getItems().length; z++) {
                            	if (ce.getLocation() < list.size())
                            		removed.add(list.remove(ce.getLocation()));
                            	else
                        			log.debug("WARN: could not restore remove for collection %s location %d", list, ce.getLocation());
                            }
                        }
                        else if (ce.getKind() == ChangeKind.REMOVE) {
                            for (int z = 0; z < ce.getItems().length; z++) {
                            	if (ce.getLocation()+z <= list.size())
                            		list.add(ce.getLocation()+z, ce.getItems()[z]);
                            	else
                        			log.debug("WARN: could not restore add for collection %s location %d", list, ce.getLocation()+z);
                            }
                        }
                        else if (ce.getKind() == ChangeKind.REPLACE) {
                            if (removed == null)
                                removed = new ArrayList<Object>();
                            for (int z = 0; z < ce.getItems().length; z++) {
                            	if (ce.getLocation()+z < list.size())
                            		removed.add(list.set(ce.getLocation()+z, ((Object[])ce.getItems()[z])[0]));
                            	else
                        			log.debug("WARN: could not restore replace for collection %s location %d", list, ce.getLocation()+z);
                            }
                        }
                    }
                    
                    // Must be here because collection reset has triggered other useless CollectionEvents
                    markNotDirty(val, parent);
                }
                for (Object o : list) {
                    if (o instanceof Identifiable)
                        resetEntity(mergeContext, o, (Identifiable)o, cache);
                }
                if (removed != null) {
                    for (Object o : removed) {
                        if (o instanceof Identifiable)
                            resetEntity(mergeContext, o, (Identifiable)o, cache);
                    }
                }
            }
            else if (val instanceof Map<?, ?> && !(val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())) {
                Map<Object, Object> map = (Map<Object, Object>)val;
                List<Change> savedArray = save != null ? (List<Change>)save.get(p) : null;
                
                if (savedArray != null) {
                    for (int a = savedArray.size()-1; a >= 0; a--) {
                        Change ce = savedArray.get(a);
                        if (ce.getKind() == ChangeKind.ADD) {
                            if (removed == null)
                                removed = new ArrayList<Object>();
                            for (int z = 0; z < ce.getItems().length; z++) {
                                removed.add(map.remove(((Object[])ce.getItems()[z])[0]));
                                removed.add(((Object[])ce.getItems()[z])[0]);
                            }
                        }
                        else if (ce.getKind() == ChangeKind.REMOVE) {
                            for (int z = 0; z < ce.getItems().length; z++)
                                map.put(((Object[])ce.getItems()[z])[0], ((Object[])ce.getItems()[z])[1]);
                        }
                        else if (ce.getKind() == ChangeKind.REPLACE) {
                            if (removed == null)
                                removed = new ArrayList<Object>();
                            for (int z = 0; z < ce.getItems().length; z++) {
                                removed.add(map.put(((Object[])ce.getItems()[z])[0], ((Object[])ce.getItems()[z])[1]));
                            }
                        }
                    }
                    
                    // Must be here because collection reset has triggered other useless CollectionEvents
                    markNotDirty(val, parent);
                }
                for (Entry<Object, Object> me : map.entrySet()) {
                    if (me.getKey() instanceof Identifiable)
                        resetEntity(mergeContext, me.getKey(), (Identifiable)me.getKey(), cache);
                    if (me.getValue() instanceof Identifiable)
                        resetEntity(mergeContext, me.getValue(), (Identifiable)me.getValue(), cache);
                }
                if (removed != null) {
                    for (Object o : removed) {
                        if (o instanceof Identifiable)
                            resetEntity(mergeContext, o, (Identifiable)o, cache);
                    }
                }
            }
            else if (save != null && (ObjectUtil.isSimple(val) || ObjectUtil.isSimple(save.get(p)))) {
                if (save.containsKey(p))
                    dataManager.setInternalProperty(entity, p, save.get(p));
            }
            else if (save != null && (val instanceof Enum || save.get(p) instanceof Enum || val instanceof Value || save.get(p) instanceof Value)) {
                if (save.containsKey(p))
                    dataManager.setInternalProperty(entity, p, save.get(p));
            } 
            else if (save != null && save.containsKey(p)) {
                if (!ObjectUtil.objectEquals(dataManager, val, save.get(p)))
                    dataManager.setInternalProperty(entity, p, save.get(p));
            }
            else if (val instanceof Identifiable)
                resetEntity(mergeContext, val, (Identifiable)val, cache);
            else if (val != null && parent != null && !ObjectUtil.isSimple(val))
                resetEntity(mergeContext, val, parent, cache);
        }
        
        // Must be here because entity reset may have triggered useless new saved properties
        markNotDirty(entity, null);
    }
    
    
    /**
     *  @private
     *  Internal implementation of entity reset all
     */ 
    public void resetAllEntities(MergeContext mergeContext, Set<Object> cache) {
        boolean found = false;
        do {
            found = false;
            for (Object entity : savedProperties.keySet()) {
                if (entity instanceof Identifiable) {
                    found = true;
                    resetEntity(mergeContext, entity, (Identifiable)entity, cache);
                    break;
                }
            }
        }
        while (found);
        
        if (dirtyCount > 0)
            log.error("Incomplete reset of context, could be a bug");
    }
    
    
    /**
     *  @private 
     *  Check if a value is empty
     *
     *  @return value is empty
     */ 
    public boolean isEmpty(Object val) {
        if (val == null)
            return true;
        else if (val instanceof String)
            return val.equals("");
        else if (val.getClass().isArray())
            return Array.getLength(val) == 0;
        else if (val instanceof Date)
            return ((Date)val).getTime() == 0L;
        else if (val instanceof List<?>)
            return ((List<?>)val).size() == 0;
        else if (val instanceof Map<?, ?>)
            return ((Map<?, ?>)val).size() == 0;
        return false; 
    }
    

    public static class Change {
        
        private ChangeKind kind;
        private int location;
        private Object[] items;
        
        public Change(ChangeKind kind, int location, Object[] items) {
            this.kind = kind;
            this.location = location;
            this.items = items;
        }
        
        public ChangeKind getKind() {
            return kind;
        }
        
        public int getLocation() {
            return location;
        }
        
        public Object[] getItems() {
            return items;
        }
        
        public void moveLocation(int offset) {
            location += offset;
        }
    }
}
