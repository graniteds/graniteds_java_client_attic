package org.granite.client.persistence.javafx;

import org.granite.client.persistence.LazyableCollection;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;


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