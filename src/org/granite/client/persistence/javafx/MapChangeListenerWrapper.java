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

import org.granite.client.persistence.LazyableCollection;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * @author William DRAI
 */
public class MapChangeListenerWrapper<K, V> implements MapChangeListener<K, V> {
    private final ObservableMap<K, V> wrappedMap;
    private final MapChangeListener<K, V> wrappedListener;
    
    public MapChangeListenerWrapper(ObservableMap<K, V> wrappedMap, MapChangeListener<K, V> wrappedListener) {
        this.wrappedMap = wrappedMap;
        this.wrappedListener = wrappedListener;
    }
    
    @Override
    public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
        if (!((LazyableCollection)wrappedMap).isInitialized())
            return;
        MapChangeListener.Change<K, V> wrappedChange = new MapChangeWrapper<K, V>(wrappedMap, change);
        wrappedListener.onChanged(wrappedChange);
    }        
}