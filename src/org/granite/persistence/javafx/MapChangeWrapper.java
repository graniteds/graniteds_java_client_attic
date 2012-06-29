package org.granite.persistence.javafx;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class MapChangeWrapper<K, V> extends MapChangeListener.Change<K, V> {            
    private final MapChangeListener.Change<? extends K, ? extends V> wrappedChange;
    
    public MapChangeWrapper(ObservableMap<K, V> map, MapChangeListener.Change<? extends K, ? extends V> wrappedChange) {
        super(map);
        this.wrappedChange = wrappedChange;
    }

    @Override
    public K getKey() {
        return wrappedChange.getKey();
    }

    @Override
    public V getValueAdded() {
        return wrappedChange.getValueAdded();
    }

    @Override
    public V getValueRemoved() {
        return wrappedChange.getValueRemoved();
    }

    @Override
    public boolean wasAdded() {
        return wrappedChange.wasAdded();
    }

    @Override
    public boolean wasRemoved() {
        return wrappedChange.wasRemoved();
    }
}