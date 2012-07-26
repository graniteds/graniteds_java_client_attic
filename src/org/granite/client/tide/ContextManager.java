package org.granite.client.tide;

import java.util.List;


public interface ContextManager {
	
    public void setInstanceStoreFactory(InstanceStoreFactory instanceStoreFactory);
    
    public void setBeanManager(BeanManager beanManager);
    
    public Context getContext();
    
    public Context getContext(String contextId);
    
    public Context getContext(String contextId, String parentContextId, boolean create);
    
    public Context newContext(String contextId, String parentContextId);
    
    public Context retrieveContext(Context sourceContext, String contextId, boolean wasConversationCreated, boolean wasConversationEnded);
    
    public void updateContextId(String previousContextId, Context context);
    
    public void destroyContext(String contextId, boolean force);
    
    public List<Context> getAllContexts();
    
    // function forEachChildContext(parentContext:Context, callback:Function, token:Object = null):void;

    public void destroyContexts(boolean force);
    
    public void destroyFinishedContexts();
    
    public void addToContextsToDestroy(String contextId);
    
    public void removeFromContextsToDestroy(String contextId);
}
