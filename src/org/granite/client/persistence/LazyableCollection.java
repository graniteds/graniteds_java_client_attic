package org.granite.client.persistence;


public interface LazyableCollection {

    public boolean isInitialized();

    public void uninitialize();

    public void initializing();

    public void initialize();
    
    public LazyableCollection clone(boolean uninitialize);
}
