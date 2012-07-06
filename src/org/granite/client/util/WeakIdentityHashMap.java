package org.granite.client.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class WeakIdentityHashMap<K, V> implements Map<K, V> {
    
    private final ReferenceQueue<K> queue = new ReferenceQueue<K>();
    private Map<IdentityWeakReference, V> map = new HashMap<IdentityWeakReference, V>();


    public void clear() {
        map.clear();
        reap();
    }

    public boolean containsKey(Object key) {
        reap();
        return map.containsKey(new IdentityWeakReference(key));
    }

    public boolean containsValue(Object value)  {
        reap();
        return map.containsValue(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        reap();
        Set<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
        for (Map.Entry<IdentityWeakReference, V> ref : map.entrySet()) {
            final K key = ref.getKey().get();
            final V value = ref.getValue();
            Map.Entry<K, V> entry = new Map.Entry<K, V>() {
                public K getKey() {
                    return key;
                }
                public V getValue() {
                    return value;
                }
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
            };
            ret.add(entry);
        }
        return Collections.unmodifiableSet(ret);
    }
    
    public Set<K> keySet() {
        reap();
        Set<K> ret = new HashSet<K>();
        for (IdentityWeakReference ref : map.keySet())
            ret.add(ref.get());
        
        return Collections.unmodifiableSet(ret);
    }

    public boolean equals(Object o) {
        return map.equals(((WeakIdentityHashMap<?, ?>)o).map);
    }

    public V get(Object key) {
        reap();
        return map.get(new IdentityWeakReference(key));
    }
    public V put(K key, V value) {
        reap();
        return map.put(new IdentityWeakReference(key), value);
    }

    public int hashCode() {
        reap();
        return map.hashCode();
    }
    
    public boolean isEmpty() {
        reap();
        return map.isEmpty();
    }
    
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }
    
    public V remove(Object key) {
        reap();
        return map.remove(new IdentityWeakReference(key));
    }
    
    public int size() {
        reap();
        return map.size();
    }
    
    public Collection<V> values() {
        reap();
        return map.values();
    }

    private synchronized void reap() {
        Object zombie = queue.poll();

        while (zombie != null) {
            @SuppressWarnings("unchecked")
            IdentityWeakReference victim = (IdentityWeakReference)zombie;
            map.remove(victim);
            zombie = queue.poll();
        }
    }

    class IdentityWeakReference extends WeakReference<K> {
        
        private final int hash;
        
        @SuppressWarnings("unchecked")
        IdentityWeakReference(Object obj) {
            super((K)obj, queue);
            hash = System.identityHashCode(obj);
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object o) {
            if (this == o)
                return true;
            
            @SuppressWarnings("unchecked")
            IdentityWeakReference ref = (IdentityWeakReference)o;
            if (this.get() == ref.get())
                return true;
            
            return false;
        }
    }
}