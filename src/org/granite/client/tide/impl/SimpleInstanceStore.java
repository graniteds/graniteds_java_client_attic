package org.granite.client.tide.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.server.Component;


public class SimpleInstanceStore implements InstanceStore {
    
	private final Context context;
	private static final String TYPED = "__TYPED__";
    private Map<String, Object> instances = new HashMap<String, Object>();
    
    public SimpleInstanceStore(Context context) {
    	this.context = context;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getNoProxy(String name) {
        Object instance = instances.get(name);
        if (instance instanceof Component)
            return null;
        return (T)instance;
    }
    
    public void set(String name, Object instance) {
    	context.initInstance(instance, name);
        instances.put(name, instance);
    }
    
    private int NUM_TYPED_INSTANCE = 1;
    
    public void set(Object instance) {
    	if (instance == null)
    		throw new NullPointerException("Cannot register null component instance");
    	context.initInstance(instance, null);
    	if (!instances.containsValue(instance))
    		instances.put(TYPED + (NUM_TYPED_INSTANCE++), instance);
    }

    @Override
    public void remove(String name) {
        instances.remove(name);
    }
    
    @Override
    public void clear() {
    	instances.clear();
	}
    
    public List<String> allNames() {
    	List<String> names = new ArrayList<String>(instances.size());
    	for (String name : instances.keySet()) {
    		if (!name.startsWith(TYPED))
    			names.add(name);
    	}
    	return names;
    }

    @SuppressWarnings("unchecked")
    public <T> T byName(String name, Context context) {
        return (T)instances.get(name);
    }
    
    protected Object createInstance() {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T byType(Class<T> type, Context context) {
        T instance = null;
        for (Object i : instances.values()) {
            if (type.isInstance(i)) {
                if (instance == null)
                    instance = (T)i;
                else
                    throw new RuntimeException("Ambiguous component definition for class " + type);
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] allByType(Class<T> type, Context context) {
        List<T> list = new ArrayList<T>();
        for (Object instance : instances.values()) {
            if (type.isInstance(instance))
                list.add((T)instance);
        }
        T[] all = (T[])Array.newInstance(type, list.size());
        return list.size() > 0 ? list.toArray(all) : null;
    }

}
