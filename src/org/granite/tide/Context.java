package org.granite.tide;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import org.granite.logging.Logger;
import org.granite.rpc.AsyncToken;
import org.granite.tide.data.DataManager;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.EntityManager.Update;
import org.granite.tide.data.EntityManagerImpl;
import org.granite.tide.data.MergeContext;
import org.granite.tide.data.RemoteInitializerImpl;
import org.granite.tide.impl.SimpleEventBus;
import org.granite.tide.impl.SimpleInstanceStore;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.rpc.ServerSession;

import flex.messaging.messages.ErrorMessage;


public class Context {
    
    static final Logger log = Logger.getLogger(Context.class);
       
    private String contextId = null;
    private boolean isContextIdFromServer = false;
    private boolean finished = false;
    
    private ContextManager contextManager = null;
    private TrackingContext trackingContext = new TrackingContext();
    
    private InstanceStore componentRegistry = new SimpleInstanceStore(this);
    
    private BeanManager beanManager = new SimpleBeanManager();
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
    
    
    public void setInstanceStore(InstanceStore componentRegistry) {
        this.componentRegistry = componentRegistry;
    }
    
    public void setBeanManager(BeanManager beanPropertyAccessor) {
        this.beanManager = beanPropertyAccessor;
    }
    
    public EventBus getEventBus() {
    	return eventBus;
    }
    
    public void setPlatform(Platform platform) {
    	this.platform = platform;
    	this.entityManager = new EntityManagerImpl("", platform.getDataManager(), null, null);
    	this.entityManager.setRemoteInitializer(new RemoteInitializerImpl(this));
    	this.eventBus = platform.getEventBus();
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
        return componentRegistry.byName(name, this);
    }
    
    public <T> T byType(Class<T> type) {
        return componentRegistry.byType(type, this);
    }

    public <T> T[] allByType(Class<T> type) {
        return componentRegistry.allByType(type, this);
    }
    
    public List<String> allNames() {
    	return componentRegistry.allNames();
    }
    
    public void set(String name, Object instance) {
    	componentRegistry.set(name, instance);
    }
    
    public void set(Object instance) {
    	componentRegistry.set(instance);
    }
    
    public void clear(boolean force) {
        // TODO
    }
    

    /**
     *  Calls a remote component
     * 
     *  @param component the target component
     *  @param op name of the called metho
     *  @param arg method parameters
     *  @param withContext add context sync data to call
     * 
     *  @return the operation token
     */
    @SuppressWarnings("unchecked")
	public <T> Future<T> callComponent(ServerSession serverSession, Component component, String operation, Object[] args, boolean withContext) {
        if (finished)
            throw new InvalidContextException(contextId, "Invalid context");
                
        log.debug("callComponent %s.%s", component.getName(), operation);
        trackingContext.traceContext();
        
        TideResponder<?> responder = null;
        if (args != null && args.length > 0 && args[args.length-1] instanceof TideResponder) {
            responder = (TideResponder<?>)args[args.length-1];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 0; i < args.length-1; i++)
            	newArgs[i] = args[i];
            args = newArgs;
        }
        
		// Force generation of uids by merging all arguments in the current context
        List<Object> argsList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			if (argsList.get(i) instanceof PropertyHolder)
				argsList.set(i, ((PropertyHolder)args[i]).getObject());
		}
		argsList = (List<Object>)entityManager.mergeExternalData(argsList);
		for (int i = 0; i < args.length; i++)
			args[i] = argsList.get(i);
		
        Method method = null;
        // TODO: improve method matching
        for (Method m : component.getClass().getMethods()) {
            if (m.getName().equals(operation)) {
                method = m;
                break;
            }
        }
        if (method != null) {
            // Call argument preprocessors if necessary before sending arguments to server
            ArgumentPreprocessor[] apps = componentRegistry.allByType(ArgumentPreprocessor.class, this);
            if (apps != null) {
                for (ArgumentPreprocessor app : apps)
                    args = app.preprocess(method, args);
            }
        }
        
        Future<T> future = null;
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            AsyncToken token = serverSession.invoke(this, component, operation, args, responder, withContext, null);
            future = beanManager.buildFutureResult(token);
        }
        finally {
            trackingContext.setEnabled(saveTracking);
        }
        
        if (withContext)
            trackingContext.clearUpdates(true);
        
        // TODO
		serverSession.call();
//        if (remoteConversation != null)
//            remoteConversation.call();
        
        return future;
    }
    
    
    public void callLater(Runnable runnable) {
    	platform.execute(runnable);
    }
    
    
    /**
     *  @private  
     *  (Almost) abstract method: manages a remote call result
     *  This should be called by the implementors at the end of the result processing
     * 
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param ires invocation result object
     *  @param result result object
     *  @param mergeWith previous value with which the result will be merged
     */
    public void internalResult(ServerSession serverSession, String componentName, String operation, InvocationResult invocationResult, Object result, Object mergeWith) {
        trackingContext.clearPendingUpdates();
        
        log.debug("result {0}", result);
        
        List<ContextUpdate> resultMap = null;
        List<Update> updates = null;
        
        try {
            trackingContext.setEnabled(false);
            
            // Clear flash context variable for Grails/Spring MVC
            componentRegistry.remove("flash");
            
            MergeContext mergeContext = entityManager.initMerge();
            mergeContext.setServerSession(serverSession);
            
            boolean mergeExternal = true;
            if (invocationResult != null) {
                mergeExternal = invocationResult.getMerge();
                
                if (invocationResult.getUpdates() != null && invocationResult.getUpdates().length > 0) {
                    updates = new ArrayList<Update>(invocationResult.getUpdates().length);
                    for (Object[] u : invocationResult.getUpdates())
                        updates.add(Update.forUpdate((String)u[0], u[1]));
                    entityManager.handleUpdates(mergeContext, null, updates);
                }
                
                // Handle scope changes
                // TODO: migrate
    //            if (_componentStore.isComponentInEvent(componentName) && invocationResult.scope == Tide.SCOPE_SESSION && !meta_isGlobal) {
    //                var instance:Object = this[componentName];
    //                this[componentName] = null;
    //                _componentStore.getDescriptor(componentName).scope = Tide.SCOPE_SESSION;
    //                this[componentName] = instance;
    //            }
                
    //            componentRegistry.setScope(componentName, ScopeType.values()[invocationResult.getScope()]);
    //            componentRegistry.setRestrict(componentName, invocationResult.getRestrict() ? RestrictMode.YES : RestrictMode.NO);
                
                resultMap = invocationResult.getResults();
                
                if (resultMap != null) {
                    log.debug("result conversationId {0}", contextId);
                    
                    // Order the results by container, i.e. 'person.contacts' has to be evaluated after 'person'
                    Collections.sort(resultMap, RESULTS_COMPARATOR);
                    
                    for (int k = 0; k < resultMap.size(); k++) {
                        ContextUpdate r = resultMap.get(k);
                        Object val = r.getValue();
                        
                        log.debug("update expression {0}: {1}", r, val);
    
                        String compName = r.getComponentName();
                        // TODO
    //                    if (compName == null && val != null) {
    //                        var t:Type = Type.forInstance(val);
    //                        compName = ComponentStore.internalNameForTypedComponent(t.name + '_' + t.id);
    //                    }
                        
                        trackingContext.addLastResult(compName 
                                + (r.getComponentClassName() != null ? "(" + r.getComponentClassName() + ")" : "") 
                                + (r.getExpression() != null ? "." + r.getExpression() : ""));
                        
                        // TODO
    //                    if (val != null) {
    //                        if (_componentStore.getDescriptor(compName).restrict == Tide.RESTRICT_UNKNOWN)
    //                            _componentStore.getDescriptor(compName).restrict = r.restrict ? Tide.RESTRICT_YES : Tide.RESTRICT_NO;
    //                        
    //                        if (_componentStore.getDescriptor(compName).scope == Tide.SCOPE_UNKNOWN)
    //                            _componentStore.getDescriptor(compName).scope = r.scope;
    //                    }
    //                    _componentStore.setComponentGlobal(compName, true);
                        
                        Object obj = componentRegistry.getNoProxy(compName);
                        String[] p = r.getExpression() != null ? r.getExpression().split("\\.") : null;
                        if (p != null && p.length > 1) {
                            for (int i = 0; i < p.length-1; i++)
                                obj = beanManager.getProperty(obj, p[i]);
                        }
    //                    else if (p.length == 0)
    //                        _componentStore.setComponentRemoteSync(compName, Tide.SYNC_BIDIRECTIONAL);
                        
                        Object previous = null;
                        String propName = null;
                        if (p != null && p.length > 0) {
                            propName = p[p.length-1];
                            
                            if (obj instanceof PropertyHolder)
                                previous = beanManager.getProperty(((PropertyHolder)obj).getObject(), propName);
                            else if (obj != null)
                                previous = beanManager.getProperty(obj, propName);
                        }
                        else
                            previous = obj;
                        
                        // Don't merge with temporary properties
                        // TODO
                        if (previous instanceof Component) //  || previous instanceof ComponentProperty)
                            previous = null;
                        
                        Expression res = new ContextResult(r.getComponentName(), r.getExpression());
                        // TODO
    //                    var res:IExpression = ComponentStore.isInternalNameForTypedComponent(compName)
    //                        ? new TypedContextExpression(compName, r.expression)
    //                        : new ContextResult(r.componentName, r.expression);
                        
    //                    if (!isGlobal() && r.getScope() == ScopeType.SESSION.ordinal())
    //                        val = parentContext.getEntityManager().mergeExternal(val, previous, res);
    //                    else
                        val = entityManager.mergeExternal(mergeContext, val, previous, res, null, null, null, false);
                        
                        if (propName != null) {
                            if (obj instanceof PropertyHolder) {
                                ((PropertyHolder)obj).setProperty(propName, val);
                            }
                            else if (obj != null)
                                beanManager.setProperty(obj, propName, val);
                        }
                        else
                            componentRegistry.set(compName, val);
                    }
                }
            }
            
            // Merges final result object
            if (result != null) {
                if (mergeExternal)
                    result = entityManager.mergeExternal(mergeContext, result, mergeWith, null, null, null, null, false);
                else
                    log.debug("skipped merge of remote result");
                if (invocationResult != null)
                    invocationResult.setResult(result);
            }
        }
        finally {
            MergeContext.destroy(entityManager);
            
            trackingContext.setEnabled(true);
        }

        // TODO
//        if (componentRegistry.isComponent("statusMessages"))
//            get("statusMessages").setFromServer(invocationResult);
        
        // Dispatch received data update events         
        if (invocationResult != null) {
            trackingContext.removeResults(resultMap);
            
            // Dispatch received data update events
            if (updates != null)
                entityManager.raiseUpdateEvents(this, updates);
            
            // Dispatch received context events
            // TODO
//            List<ContextEvent> events = invocationResult.getEvents();
//            if (events != null && events.size() > 0) {
//                for (ContextEvent event : events) {
//                    if (event.params[0] is Event)
//                        meta_dispatchEvent(event.params[0] as Event);
//                    else if (event.isTyped())
//                        meta_internalRaiseEvent("$TideEvent$" + event.eventType, event.params);
//                    else
//                        _tide.invokeObservers(this, TideModuleContext.currentModulePrefix, event.eventType, event.params);
//                }
//            }
        }
        
        log.debug("result merged into local context");
    }

    /**
     *  @private 
     *  Abstract method: manages a remote call fault
     * 
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param emsg error message
     */
    public void internalFault(String componentName, String operation, ErrorMessage emsg) {
        trackingContext.clearPendingUpdates();

        // TODO
//        if (emsg != null && emsg.getExtendedData() != null && componentRegistry.isComponent("statusMessages"))
//            get("statusMessages").setFromServer(emsg.getExtendedData());
    }
	
    
    public void markAsFinished() {
        this.finished = true;
    }
    
    /**
     *  @private
     *  Comparator for expression evaluation ordering
     * 
     *  @param r1 expression 1
     *  @param r2 expression 2
     *  @param fields unused
     * 
     *  @return comparison value
     */
    private static class ResultsComparator implements Comparator<ContextUpdate> {
        
        public int compare(ContextUpdate r1, ContextUpdate r2) {
            
            if (r1.getComponentClassName() != null && r2.getComponentClassName() != null && !r1.getComponentClassName().equals(r2.getComponentClassName()))
                return r1.getComponentClassName().compareTo(r2.getComponentClassName());
            
            if (r1.getComponentName() != r2.getComponentName())
                return r1.getComponentName().compareTo(r2.getComponentName());
            
            if (r1.getExpression() == null)
                return r2.getExpression() == null ? 0 : -1;
            
            if (r2.getExpression() == null)
                return 1;
            
            if (r1.getExpression().equals(r2.getExpression()))
                return 0;
            
            if (r1.getExpression().indexOf(r2.getExpression()) == 0)
                return 1;
            if (r2.getExpression().indexOf(r1.getExpression()) == 0)
                return -1;
            
            return r1.getExpression().compareTo(r2.getExpression()) < 0 ? -1 : 0;
        }
    }
    
    private static final ResultsComparator RESULTS_COMPARATOR = new ResultsComparator();

}
