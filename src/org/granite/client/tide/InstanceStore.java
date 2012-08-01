package org.granite.client.tide;

import java.util.List;


public interface InstanceStore {
    
    public <T> T getNoProxy(String name);
    
    public void set(String name, Object instance);

    public void set(Object instance);

    public void remove(String name);
    
    public void clear();
    
    public List<String> allNames();
    
    public <T> T byName(String name, Context context);
    
    public <T> T byType(Class<T> type, Context context);
    
    public <T> T[] allByType(Class<T> type, Context context);
}
