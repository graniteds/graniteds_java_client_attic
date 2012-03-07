package org.granite.tide.collections;

import org.granite.persistence.LazyableCollection;
import org.granite.tide.rpc.ServerSession;


public interface ManagedPersistentAssociation extends LazyableCollection {
    
    public Object getOwner();
    
    public String getPropertyName();
    
    public LazyableCollection getCollection();
    
    public void setServerSession(ServerSession serverSession);
    
    public void addListener(InitializationListener listener);
    
    public interface InitializationListener {
        
        public void initialized(ManagedPersistentAssociation collection);
        
        public void uninitialized(ManagedPersistentAssociation collection);
    }
    
    public interface InitializationCallback {
        
        public void call(ManagedPersistentAssociation collection);
    }
}
