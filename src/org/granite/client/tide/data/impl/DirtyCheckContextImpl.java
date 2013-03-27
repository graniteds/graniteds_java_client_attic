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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.DataManager.ChangeKind;
import org.granite.client.tide.data.spi.DirtyCheckContext;
import org.granite.client.tide.data.spi.EntityDescriptor;
import org.granite.client.tide.data.spi.ExpressionEvaluator.Value;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.data.spi.Wrapper;
import org.granite.client.tide.server.TrackingContext;
import org.granite.client.util.WeakIdentityHashMap;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class DirtyCheckContextImpl implements DirtyCheckContext {
    
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.DirtyCheckContextImpl");
    
    private DataManager dataManager;
    private TrackingContext trackingContext;
    private int dirtyCount = 0;
    private WeakIdentityHashMap<Object, Map<String, Object>> savedProperties = new WeakIdentityHashMap<Object, Map<String, Object>>();
    private WeakIdentityHashMap<Object, Object> unsavedEntities = new WeakIdentityHashMap<Object, Object>();
    
    
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
    
    public boolean notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity) {
        boolean newDirtyEntity = isEntityChanged(entity);
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
	
	/**
	 *  Check if the object is marked as new in the context
	 *
	 *  @param object object to check
	 * 
	 *  @return true if the object has been newly attached
	 */ 
	public boolean isUnsaved(Object object) {
		return unsavedEntities.containsKey(object);
	}
	
	public void addUnsaved(Identifiable entity) {
		unsavedEntities.put(entity, true);
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
	public boolean isEntityPropertyChanged(Identifiable entity, String propertyName, Object value) {
        Map<String, Object> source = savedProperties.get(entity);
        if (source != null)
        	return source.containsKey(propertyName) && !isSame(source.get(propertyName), value);
       	
		return !isSame(dataManager.getProperty(entity, propertyName), value);
    }
    
    
    public boolean isEntityChanged(Object entity) {
        return isEntityChanged(entity, null, null, null);
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
	public boolean isEntityChanged(Object entity, Object embedded, String propName, Object value) {
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            
            boolean dirty = false;
            
            Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
            
            EntityDescriptor desc = entity instanceof Identifiable ? dataManager.getEntityDescriptor(entity) : null;
            Map<String, Object> save = savedProperties.get(entity);
            String versionPropertyName = desc != null ? desc.getVersionPropertyName() : null;
            String dirtyPropertyName = desc != null ? desc.getDirtyPropertyName() : null;
			
			if (embedded == null)
				embedded = entity;
            
            for (String p : pval.keySet()) {
                if (p.equals(versionPropertyName) || p.equals(dirtyPropertyName))
                    continue;
                
                Object val = (entity == embedded && p.equals(propName)) ? value : pval.get(p);
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
	
	public boolean isEntityDeepChanged(Object entity) {		
		return isEntityDeepChanged(entity, null, new IdentityHashMap<Object, Boolean>());
	}
	
	private boolean isEntityDeepChanged(Object entity, Object embedded, IdentityHashMap<Object, Boolean> cache) {
		if (cache == null)
			cache = new IdentityHashMap<Object, Boolean>();
		if (cache.containsKey(entity))
			return false;
		cache.put(entity, true);
		
		boolean saveTracking = trackingContext.isEnabled();
		try {
			trackingContext.setEnabled(false);
			
            Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
            
            if (embedded == null)
            	embedded = entity;
            
            EntityDescriptor desc = entity instanceof Identifiable ? dataManager.getEntityDescriptor(entity) : null;
            Map<String, Object> save = savedProperties.get(entity);
            String versionPropertyName = desc != null ? desc.getVersionPropertyName() : null;
            String dirtyPropertyName = desc != null ? desc.getDirtyPropertyName() : null;
            
            for (String p : pval.keySet()) {
                if (p.equals(versionPropertyName) || p.equals(dirtyPropertyName))
                    continue;
                
                Object val = pval.get(p);
                Object saveval = save != null ? save.get(p) : null;
                
                if (save != null && ((val != null && (ObjectUtil.isSimple(val) || val instanceof byte[]))
                        || (saveval != null && (ObjectUtil.isSimple(saveval) || saveval instanceof byte[])))) {
                    return true;
                }
                else if (save != null && (val instanceof Value || saveval instanceof Value || val instanceof Enum || saveval instanceof Enum)) {
                    if (saveval != null && ((val == null && saveval != null) || !val.equals(saveval))) {
                        return true;
                    }
                }
                else if (save != null && (val instanceof Identifiable || saveval instanceof Identifiable)) {
                    if (saveval != null && val != save.get(p))
                        return true;
                    
                    if (isEntityDeepChanged(val, null, cache))
						return true;
                }
                else if (val instanceof List<?> || val instanceof Map<?, ?>) {
                	 if (val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())
                		 return false;
                	 
                	 @SuppressWarnings("unchecked")
                	 List<Change> savedArray = (List<Change>)saveval;
                	 if (savedArray != null && !savedArray.isEmpty())
                		 return true;
                	 
                	 if (val instanceof List<?>) {
                		 for (Object elt : (List<?>)val) {
                			 if (isEntityDeepChanged(elt, null, cache))
                				 return true;
                		 }
                	 }
                	 else if (val instanceof Map<?, ?>) {
						for (Entry<?, ?> me : ((Map<?, ?>)val).entrySet()) {
							if (isEntityDeepChanged(me.getKey(), null, cache))
								return true;
							if (isEntityDeepChanged(me.getValue(), null, cache))
								return true;
						}
					}
                }
                else if (val != null
                    && !(val instanceof Identifiable || val instanceof Enum || val instanceof Value || val instanceof byte[]) 
                    && isEntityDeepChanged(val, embedded, cache)) {
                    return true;
                }
			}
		}
		finally {            
			trackingContext.setEnabled(saveTracking);
		}
		
		return false;
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

    private boolean isSameList(List<Object> save, Collection<?> coll) {
    	if (save.size() != coll.size())
    		return false;

    	if (coll instanceof List<?>) {
    		List<?> list = (List<?>)coll;
	        for (int i = 0; i < save.size(); i++) {
	        	if (!isSame(save.get(i), list.get(i)))
	        		return false;
	        }
    	}
    	else {
    		for (Object e : save) {
    			if (!coll.contains(e))
    				return false;
    		}
    	}
        return true;
    }    
    
    private boolean isSameMap(List<Object[]> save, Map<?, ?> map) {
    	if (save.size() != map.size())
    		return false;

        for (int i = 0; i < save.size(); i++) {
        	Object[] entry = save.get(i);
        	if (!map.containsKey(entry[0]))
        		return false;
        	if (!isSame(entry[1], map.get(entry[0])))
        		return false;
        }
        return true;
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
            	Object key = null;
                for (Object f : map2.keySet()) {
                    if (isSameExt(e, f)) {
                        key = f;
                        break;
                    }
                }
                if (key == null)
                    return false;
                if (!isSameExt(map1.get(e), map2.get(key)))
                    return false;
            }
            for (Object f : map2.keySet()) {
            	Object key = null;
                for (Object e : map1.keySet()) {
                    if (isSameExt(e, f)) {
                        key = e;
                        break;
                    }
                }
                if (key == null)
                    return false;
                if (!isSameExt(map1.get(key), map2.get(f)))
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
            boolean oldDirtyEntity = isEntityChanged(entity, object, propName, oldValue);
            
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
            
            notifyEntityDirtyChange(entity, oldDirtyEntity);
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
	public void entityCollectionChangeHandler(Object owner, String propName, Collection<?> coll, ChangeKind kind, Integer location, Object[] items) {
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
    
        List<Object> save = (List<Object>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Object>();
            esave.put(propName, save);
						
			// Save collection snapshot
			for (Object e : coll)
				save.add(e);
			
			// Adjust with last event
			if (kind == ChangeKind.ADD) {
				if (location != null) {
					for (int i = 0; i < items.length; i++)
						save.remove(location.intValue());
				}
				else {
					for (Object item : items)
						save.remove(item);
				}
			}
			else if (kind == ChangeKind.REMOVE) {
				if (location != null) {
					for (int i = 0; i < items.length; i++)
						save.add(location.intValue()+i, items[i]);
				}
				else {
					for (Object item : items)
						save.add(item);
				}
			}
			else if (kind == ChangeKind.REPLACE) {
				if (location != null)
					save.set(location.intValue(), ((Object[])items[0])[0]);
				else {
					save.remove(((Object[])items[0])[1]);
					save.add(((Object[])items[0])[0]);
				}
			}
        }
		else {
			if (isSameList(save, coll)) {
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
		}
        
        notifyEntityDirtyChange(owner, oldDirtyEntity);
        
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
	public void entityMapChangeHandler(Object owner, String propName, Map<?, ?> map, ChangeKind kind, Object[] items) {
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
        
        List<Object[]> save = (List<Object[]>)esave.get(propName);
        if (save == null) {
            save = new ArrayList<Object[]>();
            esave.put(propName, save);

			// Save map snapshot
			for (Entry<?, ?> entry : map.entrySet()) {
				boolean found = false;
				if (kind == ChangeKind.ADD) {
					for (Object item : items) {
						if (isSame(entry.getKey(), ((Object[])item)[0])) {
							found = true;
							break;
						}
					}
				}
				else if (kind == ChangeKind.REPLACE) {
					for (Object item : items) {
						if (isSame(entry.getKey(), ((Object[])item)[0])) {
							save.add(new Object[] { entry.getKey(), ((Object[])item)[1] });
							found = true;
							break;
						}
					}
				}
				if (!found)
					save.add(new Object[] { entry.getKey(), entry.getValue() });
			}
			
			// Add removed element if needed
			if (kind == ChangeKind.REMOVE) {
				for (Object item : items)
					save.add(new Object[] { ((Object[])item)[0], ((Object[])item)[1] });
			}
        }
		else {
			if (isSameMap(save, map)) {
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
		}
        
        notifyEntityDirtyChange(owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
    }


    /**
     *  @private 
     *  Mark an object merged from the server as not dirty
     *
     *  @param object merged object
     */ 
    public void markNotDirty(Object object, Identifiable entity) {
    	if (entity != null)
    		unsavedEntities.remove(entity);
    	
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
            notifyEntityDirtyChange(entity, oldDirtyEntity);
        
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
     *  @param owner owner entity for embedded objects
     *  @return true if the entity is still dirty after comparing with incoming object
     */ 
    public boolean checkAndMarkNotDirty(MergeContext mergeContext, Object entity, Object source, Object parent) {
    	if (entity != null)
    		unsavedEntities.remove(entity);
    	
    	Map<String, Object> save = savedProperties.get(entity);
        if (save == null)
            return false;
        
		Object owner = entity instanceof Identifiable ? (Identifiable)entity : parent;
		
        boolean oldDirty = isDirty();
        boolean oldDirtyEntity = isEntityChanged(owner);
        
		List<String> merged = new ArrayList<String>();
		
        EntityDescriptor desc = owner instanceof Identifiable ? dataManager.getEntityDescriptor(owner) : null;
        String versionPropertyName = desc != null ? desc.getVersionPropertyName() : null;
		
		if (source instanceof Identifiable && versionPropertyName != null)
			save.put(versionPropertyName, dataManager.getProperty(source, versionPropertyName));
		
		Map<String, Object> pval = dataManager.getPropertyValues(entity, false, false);
		for (String propName : pval.keySet()) {
			if (propName.equals(versionPropertyName) || propName.equals("dirty"))
				continue;
			
//			if (!source.hasOwnProperty(propName))
//				continue;
			
			Object localValue = pval.get(propName);
			if (localValue instanceof PropertyHolder)
				localValue = ((PropertyHolder)localValue).getObject();
			
			Object sourceValue = dataManager.getProperty(source, propName);
			
			if (isSameExt(sourceValue, localValue)) {
				merged.add(propName);
				continue;
			}
			
			if (sourceValue == null || ObjectUtil.isSimple(sourceValue) || sourceValue instanceof Value || sourceValue instanceof Enum) {
				save.put(propName, sourceValue);
			}
			else if (sourceValue instanceof Identifiable) {
				save.put(propName, mergeContext.getFromCache(sourceValue));
			}
			else if (sourceValue instanceof Collection<?> && !(sourceValue instanceof LazyableCollection && !((LazyableCollection)sourceValue).isInitialized())) {
				List<Object> snapshot = new ArrayList<Object>((Collection<?>)sourceValue);
				save.put(propName, snapshot);
			}
			else if (sourceValue instanceof Map<?, ?> && !(sourceValue instanceof LazyableCollection && !((LazyableCollection)sourceValue).isInitialized())) {
				Map<?, ?> map = (Map<?, ?>)sourceValue;
				List<Object[]> snapshot = new ArrayList<Object[]>(map.size());
				for (Entry<?, ?> entry : map.entrySet())
					snapshot.add(new Object[] { entry.getKey(), entry.getValue() });
				save.put(propName, snapshot);
			}
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
        
        boolean newDirtyEntity = notifyEntityDirtyChange(owner, oldDirtyEntity);
        
        notifyDirtyChange(oldDirty);
        
        return newDirtyEntity;
    }
	
	
	public void fixRemovalsAndPersists(MergeContext mergeContext, List<Object> removals, List<Object> persists) {
		boolean oldDirty = dirtyCount > 0;
		
		for (Object object : savedProperties.keySet()) {
			Identifiable owner = null;
			if (object instanceof Identifiable)
				owner = (Identifiable)object;
			else {
				Object[] ownerEntity = mergeContext.getOwnerEntity(object);
				if (ownerEntity != null && ownerEntity[0] instanceof Identifiable)
					owner = (Identifiable)ownerEntity[0];
			}
			
			EntityDescriptor desc = dataManager.getEntityDescriptor(owner);
			
			boolean oldDirtyEntity = isEntityChanged(object);
			
	    	Map<String, Object> save = savedProperties.get(object);
			
	    	Iterator<String> ip = save.keySet().iterator();
			while (ip.hasNext()) {
				String p = ip.next();
				Object sn = save.get(p);
				if (!(sn instanceof List<?>))
					continue;
				
				Object value = dataManager.getProperty(object, p);
				if (value instanceof Collection<?>) {
					@SuppressWarnings("unchecked")
					List<Object> snapshot = (List<Object>)sn;
					Collection<?> coll = (Collection<?>)value;
					if (removals != null) {
						Iterator<Object> isne = snapshot.iterator();
						while (isne.hasNext()) {
							Object sne = isne.next();
							for (Object removal : removals) {
								if (sne instanceof Identifiable && ObjectUtil.objectEquals(dataManager, sne, removal))
									isne.remove();
							}
						}
					}
					if (persists != null) {
						for (Object persist : persists) {
							if (coll instanceof List<?>) {
								List<?> list = (List<?>)coll;
								List<Integer> found = new ArrayList<Integer>();
								for (int j = 0; j < list.size(); j++) {
									if (ObjectUtil.objectEquals(dataManager, list.get(j), persist))
										found.add(j);
								}
								for (int j = 0; j < snapshot.size(); j++) {
									if (ObjectUtil.objectEquals(dataManager, persist, snapshot.get(j))) {
										snapshot.remove(j);
										j--;
									}
								}
								for (int idx : found)
									snapshot.add(idx, persist);
							}
							else {
								if (coll.contains(persist) && !snapshot.contains(persist))
									snapshot.add(persist);
							}
						}
					}
					
					if (isSameList(snapshot, coll))
						ip.remove();
				}
				else if (value instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					List<Object[]> snapshot = (List<Object[]>)sn;
					Map<?, ?> map = (Map<?, ?>)value;
					if (removals != null) {
						Iterator<Object[]> isne = snapshot.iterator();
						while (isne.hasNext()) {
							Object[] sne = isne.next();
							for (Object removal : removals) {
								if (sne[0] instanceof Identifiable && ObjectUtil.objectEquals(dataManager, sne[0], removal))
									isne.remove();
								else if (sne[1] instanceof Identifiable && ObjectUtil.objectEquals(dataManager, sne[1], removal))
									isne.remove();
							}
						}
					}
					// TODO: persist ?				
//					if (persists != null) {
//						for (Object persist : persists) {
//							boolean foundKey = false;
//							List<Object> foundValues = new ArrayList<Object>();
//							Iterator<Object[]> isne = snapshot.iterator();
//							while (isne.hasNext()) {
//								Object[] sne = isne.next();
//								if (ObjectUtil.objectEquals(dataManager, sne[0], persist))
//									foundKey = true;
//								if (ObjectUtil.objectEquals(dataManager, sne[1], persist)) {
//									foundValues.add(sne[0]);
//									isne.remove();
//								}
//							}
//							if (map.containsKey(persist) && !foundKey)
//								snapshot.add(new Object[] { persist, map.get(persist) });
//							if (map.containsValue(persist)) {
//								for (Entry<?, ?> e : map.entrySet()) {
//									if (e.getValue().equals(persist) && !e.getKey().equals(persist))
//										snapshot.add(new Object[] { e.getKey(), e.getValue() });
//								}
//							}
//						}
//					}
					
					if (isSameMap(snapshot, map))
						ip.remove();
				}
			}
			
            int count = 0;
            for (Object p : save.keySet()) {
                if (!p.equals(desc.getVersionPropertyName()))
                    count++;
            }
            if (count == 0) {
                savedProperties.remove(object);
                dirtyCount--;
            }
			
			notifyEntityDirtyChange(object, oldDirtyEntity);
		}
		
		notifyDirtyChange(oldDirty);
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
            
            Object val = dataManager.getProperty(entity, p);
            if (val instanceof Collection<?> && !(val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())) {
                Collection<Object> coll = (Collection<Object>)val;
                List<Object> savedArray = save != null ? (List<Object>)save.get(p) : null;
                
                if (savedArray != null) {
                	for (Object obj : coll) {
                		if (obj instanceof Identifiable)
                			resetEntity(mergeContext, obj, parent, cache);
                	}
                	coll.clear();
                	for (Object e : savedArray)
                		coll.add(e);
                    
                    // Must be here because collection reset has triggered other useless CollectionEvents
                    markNotDirty(val, parent);
                }
                
                for (Object o : coll) {
                    if (o instanceof Identifiable)
                        resetEntity(mergeContext, o, (Identifiable)o, cache);
                }
            }
            else if (val instanceof Map<?, ?> && !(val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())) {
                Map<Object, Object> map = (Map<Object, Object>)val;
                List<Object[]> savedArray = save != null ? (List<Object[]>)save.get(p) : null;
                
                if (savedArray != null) {
                	for (Entry<Object, Object> entry : map.entrySet()) {
                		if (entry.getKey() instanceof Identifiable)
                			resetEntity(mergeContext, entry.getKey(), parent, cache);
                		if (entry.getValue() instanceof Identifiable)
                			resetEntity(mergeContext, entry.getValue(), parent, cache);
		            }
            		map.clear();
            		for (Object[] e : savedArray)
            			map.put(e[0], e[1]);
                    
                    // Must be here because collection reset has triggered other useless CollectionEvents
                    markNotDirty(val, parent);
                }
                
                for (Entry<Object, Object> me : map.entrySet()) {
                    if (me.getKey() instanceof Identifiable)
                        resetEntity(mergeContext, me.getKey(), (Identifiable)me.getKey(), cache);
                    if (me.getValue() instanceof Identifiable)
                        resetEntity(mergeContext, me.getValue(), (Identifiable)me.getValue(), cache);
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
