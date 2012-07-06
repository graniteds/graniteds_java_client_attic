package org.granite.client.persistence.javafx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.granite.client.persistence.LazyableCollection;
import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;


@RemoteClass("org.granite.messaging.persistence.ExternalizablePersistentMap")
public class PersistentMap<K, V> implements ObservableMap<K, V>, LazyableCollection, Externalizable {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistentMap.class);

    @SuppressWarnings("unused")
	private boolean initializing = false;
    private boolean initialized = false;
    private String metadata = null;
    private boolean dirty = false;
    
    private ObservableMap<K, V> omap;
   
    private MapChangeListener<K, V> listener = new MapChangeListener<K, V>() {
        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            if (!initialized)
                return;
            if (change.wasAdded() || change.wasRemoved())
                dirty = true;
        }
    };

    
    public PersistentMap() {
        this.omap = FXCollections.observableHashMap();
        this.initialized = true;
        addListener(listener);
    }

    public PersistentMap(Map<K, V> map) {
        this.omap = FXCollections.observableMap(map);
        this.initialized = true;
        addListener(listener);
    }
    
    public PersistentMap(boolean initialized) {
        this.omap = FXCollections.observableHashMap();
        this.initialized = initialized;         
        if (initialized)
            addListener(listener);
    }


    public final boolean isInitialized() {
        return initialized;
    }

    public void initializing() {
        clear();
        initializing = true;
        dirty = false;
        removeListener(listener);
    }

    public void initialize() {
        initializing = false;
        initialized = true;
        dirty = false;
        addListener(listener);
    }

    public void uninitialize() {
        removeListener(listener);
        initialized = false;
        clear();
        dirty = false;
    }
    
    public PersistentMap<K, V> clone(boolean uninitialize) {
        PersistentMap<K, V> map = new PersistentMap<K, V>(initialized && !uninitialize);
        map.metadata = metadata;
        if (initialized) {
            for (Entry<K, V> me : omap.entrySet())
                map.put(me.getKey(), me.getValue());
        }
        map.dirty = dirty;
        return map; 
    }
    

    public void addListener(InvalidationListener listener) {
        omap.addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
        omap.removeListener(listener);
    }
    
    private Map<MapChangeListener<? super K, ? super V>, MapChangeListener<? super K, ? super V>> listenerWrappers = 
        new IdentityHashMap<MapChangeListener<? super K, ? super V>, MapChangeListener<? super K, ? super V>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addListener(MapChangeListener<? super K, ? super V> listener) {
        MapChangeListener<? super K, ? super V> listenerWrapper = new MapChangeListenerWrapper(this, listener);
        listenerWrappers.put(listener, listenerWrapper);
        omap.addListener(listenerWrapper);
    }

    public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        MapChangeListener<? super K, ? super V> listenerWrapper = listenerWrappers.remove(listener);
        if (listenerWrapper != null)
        	omap.removeListener(listenerWrapper);
    }
    

    public boolean isEmpty() {
        return omap.isEmpty();
    }
    
    // TODO: should probably use a wrapper for the underlying set
    public Set<K> keySet() {
        return omap.keySet();
    }
    
    public Set<Entry<K, V>> entrySet() {
        return omap.entrySet();
    }
    
    public Collection<V> values() {
        return omap.values();
    }
    
    public boolean containsKey(Object o) {
        return omap.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return omap.containsValue(o);
    }

    public V get(Object key) {
        return omap.get(key);
    }

    public V put(K key, V value) {
        return omap.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        omap.putAll(map);
    }

    public V remove(Object o) {
        return omap.remove(o);
    }

    public void clear() {
        omap.clear();
    }

    public int size() {
        return omap.size();
    }

    public boolean equals(Object o) {
        return omap.equals(o);
    }

    public int hashCode() {
        return omap.hashCode();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + (initialized ? "" : " (uninitialized)") + (dirty ? " (dirty)" : "") + ":" + omap.toString();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        initialized = input.readBoolean();
        metadata = (String)input.readObject();
        if (initialized) {
            dirty = input.readBoolean();
            omap = FXCollections.observableMap((Map<K, V>)input.readObject());
        }
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeBoolean(initialized);
        output.writeObject(metadata);
        if (initialized) {
            output.writeBoolean(dirty);
            output.writeObject(new HashMap<K, V>(omap));
        }
    }
}
