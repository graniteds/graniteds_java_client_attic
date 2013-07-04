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

package org.granite.client.tide.collections.javafx;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentMap;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public class JavaFXManagedPersistentMap<K, V> extends AbstractJavaFXManagedPersistentAssociation implements ObservableMap<K, V>, ManagedPersistentMap<K, V> {
    
    private final ObservableMap<K, V> map;
    
    
    @SuppressWarnings("unchecked")
	public JavaFXManagedPersistentMap(Object entity, String propertyName, LazyableCollection map) {
        super(entity, propertyName);
        this.map = (ObservableMap<K, V>)map;
    }
        
    public Object getObject() {
        return map;
    }

    @Override
    public LazyableCollection getCollection() {
        return (LazyableCollection)map;
    }
    
    public int size() {
        if (checkForRead())
            return map.size();
        return 0;
    }

    public boolean isEmpty() {
        if (checkForRead())
            return map.isEmpty();
        return true;
    }

    @Override
    public void clear() {
        boolean wasInitialized = checkForRead(false);
        map.clear();
        if (!wasInitialized)
            initialize();
    }
    
    public V get(Object key) {
        if (checkForRead())
            return map.get(key);
        return null;
    }

    @Override
    public boolean containsKey(Object item) {
        if (checkForRead())
            return map.containsKey(item);
        return false;
    }

    @Override
    public boolean containsValue(Object item) {
        if (checkForRead())
            return map.containsValue(item);
        return false;
    }

    @Override
    public Set<K> keySet() {
        if (checkForRead())
            return map.keySet();
        return null;
    }

    @Override
    public Collection<V> values() {
        if (checkForRead())
            return map.values();
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (checkForRead())
            return map.entrySet();
        return null;
    }
    
    @Override
    public V put(K key, V value) {
        checkForWrite();
        return map.put(key, value);
    }
    
    public void putAll(Map<? extends K, ? extends V> m) {
        checkForWrite();
        map.putAll(m);
    }
    
    public V remove(Object key) {
        checkForWrite();
        return map.remove(key);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        map.addListener(listener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super V> listener) {
        map.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        map.removeListener(listener);
    }

    @Override
    public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        map.removeListener(listener);
    }

    @Override
    public LazyableCollection clone(boolean uninitialize) {
        return new JavaFXManagedPersistentMap<K, V>(getOwner(), getPropertyName(), ((LazyableCollection)map).clone(uninitialize));
    }
}