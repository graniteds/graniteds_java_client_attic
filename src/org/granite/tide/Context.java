package org.granite.tide;

import java.util.List;

import org.granite.logging.Logger;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.impl.EntityManagerImpl;
import org.granite.tide.data.impl.RemoteInitializerImpl;
import org.granite.tide.data.spi.DataManager;
import org.granite.tide.impl.DefaultPlatform;
import org.granite.tide.impl.SimpleEventBus;
import org.granite.tide.impl.SimpleInstanceStore;


public class Context {
    
    static final Logger log = Logger.getLogger(Context.class);
       
    private String contextId = null;
    private boolean isContextIdFromServer = false;
    private boolean finished = false;
    
    private ContextManager contextManager = null;
    
    private InstanceStore instanceStore = new SimpleInstanceStore(this);
    private BeanManager beanManager;
    
    private Platform platform = new DefaultPlatform();
	private EventBus eventBus = new SimpleEventBus();
    
    private EntityManager entityManager;
    
    
    
    public Context(ContextManager contextManager, Context parentCtx, String contextId) {
        this.contextManager = contextManager;
        // TODO : parentCtx
        this.contextId = contextId;
    }
    
    public ContextManager getContextManager() {
    	return contextManager;
    }
    
    public EntityManager getEntityManager() {
    	return entityManager;
    }
    
    public DataManager getDataManager() {
    	return platform.getDataManager();
    }
    
    
    public void initContext(Platform platform, BeanManager beanManager, InstanceStore instanceStore) {
    	this.platform = platform;
    	this.entityManager = new EntityManagerImpl("", platform.getDataManager(), null, null);
    	this.entityManager.setRemoteInitializer(new RemoteInitializerImpl(this));
    	this.eventBus = platform.getEventBus();
        this.instanceStore = instanceStore;
        this.beanManager = beanManager;
    }
    
    public EventBus getEventBus() {
    	return eventBus;
    }
    
    public BeanManager getBeanManager() {
    	return beanManager;
    }
    
    public void postInit() {
        // TODO
    }
    
    public Context getParentContext() {
        return null;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    public boolean isContextIdFromServer() {
        return isContextIdFromServer;
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    /**
     *  @private
     *  Update the context id
     *  @param contextId context id
     *  @param fromServer is this id received from the server ?
     */
    public void setContextId(String contextId, boolean fromServer) {
        String previousContextId = this.contextId;
        this.contextId = contextId;
        // TODO
//        if (_remoteConversation != null)
//            _remoteConversation.id = contextId;
        this.isContextIdFromServer = fromServer;
        contextManager.updateContextId(previousContextId, this);
    }

    public <T> T byName(String name) {
        return instanceStore.byName(name, this);
    }
    
    public <T> T byNameNoProxy(String name) {
    	return instanceStore.getNoProxy(name);
    }
    
    public <T> T byType(Class<T> type) {
        return instanceStore.byType(type, this);
    }

    public <T> T[] allByType(Class<T> type) {
        return instanceStore.allByType(type, this);
    }
    
    public List<String> allNames() {
    	return instanceStore.allNames();
    }
    
    public void set(String name, Object instance) {
    	instanceStore.set(name, instance);
    }
    
    public void set(Object instance) {
    	instanceStore.set(instance);
    }
    
    public void remove(String name) {
    	instanceStore.remove(name);
    }
    
    public void clear(boolean force) {
        // TODO
    }
    
    public void initInstance(Object instance, String name) {
    	if (name != null && instance instanceof NameAware)
    		((NameAware)instance).setName(name);
    	if (instance instanceof ContextAware)
    		((ContextAware)instance).setContext(this);
    	if (instance instanceof Initializable)
    		((Initializable)instance).init();
    }
    
    
    
    public void checkValid() {
    	if (finished)
            throw new InvalidContextException(contextId, "Invalid context");
    }
    
    
    public void callLater(Runnable runnable) {
    	platform.execute(runnable);
    }
    	
    
    public void markAsFinished() {
        this.finished = true;
    }
}
