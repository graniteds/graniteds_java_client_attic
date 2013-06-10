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

package org.granite.client.persistence.javafx;

import java.io.Externalizable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class PersistentMap<K, V> extends org.granite.client.persistence.collection.PersistentMap<K, V> implements ObservableMap<K, V>, Externalizable {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistentMap.class);

    
    public PersistentMap() {
    	this(true);
    }

    public PersistentMap(Map<K, V> map) {
    	super(FXCollections.observableMap(map));
    }
    
    public PersistentMap(boolean initialized) {
    	super();
    	if (initialized)
    		init(FXCollections.observableMap(new HashMap<K, V>()), false);
    }
    
    @Override
    public ObservableMap<K, V> getCollection() {
    	return (ObservableMap<K, V>)super.getCollection();
    }
   

    public void addListener(InvalidationListener listener) {
        getCollection().addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
    	getCollection().removeListener(listener);
    }
    
    private Map<MapChangeListener<? super K, ? super V>, MapChangeListener<? super K, ? super V>> listenerWrappers = 
        new IdentityHashMap<MapChangeListener<? super K, ? super V>, MapChangeListener<? super K, ? super V>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addListener(MapChangeListener<? super K, ? super V> listener) {
        MapChangeListener<? super K, ? super V> listenerWrapper = new MapChangeListenerWrapper(this, listener);
        listenerWrappers.put(listener, listenerWrapper);
        getCollection().addListener(listenerWrapper);
    }

    public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        MapChangeListener<? super K, ? super V> listenerWrapper = listenerWrappers.remove(listener);
        if (listenerWrapper != null)
        	getCollection().removeListener(listenerWrapper);
    }
}
