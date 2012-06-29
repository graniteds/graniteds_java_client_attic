package org.granite.tide.collections.javafx;

import java.util.ArrayList;
import java.util.List;

import org.granite.logging.Logger;
import org.granite.persistence.LazyableCollection;
import org.granite.rpc.events.ResultEvent;
import org.granite.tide.PropertyHolder;
import org.granite.tide.collections.ManagedPersistentAssociation;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.PersistenceManager;
import org.granite.tide.data.impl.ObjectUtil;
import org.granite.tide.data.spi.Wrapper;
import org.granite.tide.server.ServerSession;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public abstract class AbstractJavaFXManagedPersistentAssociation implements ManagedPersistentAssociation, PropertyHolder, Wrapper {
    
    private static Logger log = Logger.getLogger("org.granite.tide.javafx.AbstractJavaFXManagedPersistentAssociation");
    
    private final Identifiable entity;
    private final String propertyName;
    
    private ServerSession serverSession = null;

    private boolean localInitializing = false;
    private boolean initializing = false;
    private List<InitializationListener> listeners = new ArrayList<InitializationListener>();
    private InitializationCallback initializationCallback = null;
    
    
    public Identifiable getOwner() {
        return entity;
    } 
    
    public String getPropertyName() {
        return propertyName;
    }
    
    protected AbstractJavaFXManagedPersistentAssociation(Identifiable entity, String propertyName) {
        this.entity = entity;
        this.propertyName = propertyName;
    }
    
    public void setServerSession(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
    }
        
    public Object getWrappedObject() {
        return getObject();
    }
    
    public void propertyResultHandler(String propName, ResultEvent event) {
    }
    
    public void setProperty(String propertyName, Object value) {
    }
    
    public boolean isInitialized() {
        return ((LazyableCollection)getObject()).isInitialized();
    }
    
    public boolean isInitializing() {
        return initializing;
    }
    
    public void initializing() {
        ((LazyableCollection)getObject()).initializing();
        localInitializing = true;
    }
    
    public void addListener(InitializationListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    
    public void removeListener(InitializationListener listener) {
        listeners.remove(listener);
    }
    
    private void requestInitialization() {
        if (localInitializing)
            return;
        
        EntityManager entityManager = PersistenceManager.getEntityManager(entity);
        if (!initializing && entityManager.initializeObject(serverSession, this))                
            initializing = true;
    }
    
    protected boolean checkForRead() {
        return checkForRead(true);
    }
    protected boolean checkForRead(boolean requestInitialization) {
        if (!localInitializing && !isInitialized()) {
            if (requestInitialization)
                requestInitialization();
            return false;
        }
        return true;
    }
    protected void checkForWrite() {
        if (!localInitializing && !isInitialized())
            throw new IllegalStateException("Cannot modify uninitialized association: " + getOwner() + " property " + getPropertyName()); 
    }
    
    public void initialize() {
        ((LazyableCollection)getObject()).initialize();
        localInitializing = false;
        
        if (initializing) {
            log.debug("notify item pending");
            // TODO: check JavaFX mechanism for deferred loading
//            ResultEvent event = new ResultEvent(null, null);
//            
//            if (_itemPendingError.responders) {
//                for (var k:int = 0; k < _itemPendingError.responders.length; k++)
//                    _itemPendingError.responders[k].result(event);
//            }
            
            initializing = false;
        }
        
        for (InitializationListener listener : listeners)
            listener.initialized(this);
        
        log.debug("initialized");
    }
    
    public void uninitialize() {
        ((LazyableCollection)getObject()).uninitialize();
        initializing = false;
        initializationCallback = null;
        localInitializing = false;
        
        for (InitializationListener listener : listeners)
            listener.uninitialized(this);
    }
        
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + ObjectUtil.toString(entity) + "." + getPropertyName() + ": " + getObject().toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(getClass()))
            return false;
        
        AbstractJavaFXManagedPersistentAssociation mass = (AbstractJavaFXManagedPersistentAssociation)obj;
        return entity.equals(mass.entity) && propertyName.equals(mass.propertyName);
    }
    
    @Override
    public int hashCode() {
        int hashCode = entity.hashCode();
        hashCode = 37 * hashCode + propertyName.hashCode();
        return hashCode;
    }
    
    public void withInitialized(InitializationCallback callback) {
        if (isInitialized())
            initializationCallback.call(this);
        else {
            initializationCallback = callback;
            requestInitialization();
        }
    }
}