package org.granite.tide.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;
import org.granite.rpc.AsyncToken;
import org.granite.rpc.InvocationInterceptor;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.rpc.remoting.RemoteObject;
import org.granite.tide.ArgumentPreprocessor;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.ContextAware;
import org.granite.tide.NameAware;
import org.granite.tide.PropertyHolder;
import org.granite.tide.TideResponder;
import org.granite.tide.TrackingContext;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.server.ComponentResponder;
import org.granite.tide.server.ServerSession;


public class ComponentImpl implements Component, ContextAware, NameAware {
    
	private static final Logger log = Logger.getLogger(ComponentImpl.class);


    private String name;
    private Context context;
    private final ServerSession serverSession;
    
    
    public ComponentImpl(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    
    public void setName(String name) {
    	this.name = name;
    }
    public String getName() {
    	return name;
    }
    
    public void setContext(Context context) {
    	this.context = context;
    }
    
    protected Context getContext() {
    	return context;
    }
    
    protected ServerSession getServerSession() {
    	return serverSession;
    }
    
    
    @SuppressWarnings("unchecked")
    public <T> Future<T> call(String operation, Object... args) {
        Context context = this.context;
        
        if (args != null && args.length > 0 && args[0] instanceof Context) {
            context = (Context)args[0];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 1; i < args.length-1; i++)
            	newArgs[i-1] = args[i];
            args = newArgs;
        }
        
        return (Future<T>)callComponent(context, operation, args, false);
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
	protected <T> Future<T> callComponent(Context context, String operation, Object[] args, boolean withContext) {
    	context.checkValid();
        
        log.debug("callComponent %s.%s", getName(), operation);
        
        TideResponder<?> responder = null;
        if (args != null && args.length > 0 && args[args.length-1] instanceof TideResponder) {
            responder = (TideResponder<?>)args[args.length-1];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 0; i < args.length-1; i++)
            	newArgs[i] = args[i];
            args = newArgs;
        }
        
		// Force generation of uids by merging all arguments in the current context
        context.getEntityManager().initMerge();
        List<Object> argsList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			if (argsList.get(i) instanceof PropertyHolder)
				argsList.set(i, ((PropertyHolder)args[i]).getObject());
		}
		argsList = (List<Object>)context.getEntityManager().mergeExternalData(argsList);
		for (int i = 0; i < args.length; i++)
			args[i] = argsList.get(i);
		
        Method method = null;
        // TODO: improve method matching
        for (Method m : getClass().getMethods()) {
            if (m.getName().equals(operation)) {
                method = m;
                break;
            }
        }
        if (method != null) {
            // Call argument preprocessors if necessary before sending arguments to server
            ArgumentPreprocessor[] apps = context.allByType(ArgumentPreprocessor.class);
            if (apps != null) {
                for (ArgumentPreprocessor app : apps)
                    args = app.preprocess(method, args);
            }
        }
        
        TrackingContext trackingContext = serverSession.getTrackingContext();
        Future<T> future = null;
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            AsyncToken token = invoke(context, this, operation, args, responder, withContext, null);
            future = context.getBeanManager().buildFutureResult(token);
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
    
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AsyncToken invoke(Context context, Component component, String operation, Object[] args, TideResponder<?> tideResponder, 
                           boolean withContext, ComponentResponderImpl.Handler handler) {
        log.debug("invokeComponent %s > %s.%s", context.getContextId(), component.getName(), operation);
        
        ComponentResponder.Handler h = handler != null ? handler : new ComponentResponder.Handler() {            
			@Override
            public void result(Context context, ResultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder) {
            	context.callLater(new ResultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
            
            @Override
            public void fault(Context context, FaultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder) {
            	context.callLater(new FaultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
        };
        ComponentResponder componentResponder = new ComponentResponderImpl(context, h, component, operation, args, null, tideResponder);
        
        InvocationInterceptor[] interceptors = context.allByType(InvocationInterceptor.class);
        if (interceptors != null) {
            for (InvocationInterceptor interceptor : interceptors)
                interceptor.beforeInvocation(context, component, operation, args, componentResponder);
        }
        
        context.getContextManager().destroyFinishedContexts();
        
//        // Force generation of uids by merging all arguments in the current context
//        for (int i = 0; i < args.length; i++) {
//            if (args[i] instanceof PropertyHolder)
//                args[i] = ((PropertyHolder)args[i]).getObject();
//            args[i] = entityManager.mergeExternal(args[i], null);
//        }
//        
//        // Call argument preprocessors before sending arguments to server
//        var method:Method = Type.forInstance(component).getInstanceMethodNoCache(op);
//        for each (var app:IArgumentPreprocessor in allByType(IArgumentPreprocessor, true))
//            componentResponder.args = app.preprocess(method, args);
        
    	Object[] call = new Object[5];
    	call[0] = componentResponder.getComponent().getName();
    	String componentClassName = null;
    	if (componentResponder.getComponent().getClass() != ComponentImpl.class) {
    		RemoteClass remoteClass = componentResponder.getComponent().getClass().getAnnotation(RemoteClass.class);
    		componentClassName = remoteClass != null ? remoteClass.value() : componentResponder.getComponent().getClass().getName();
    	}
    	call[1] = componentClassName;
    	call[2] = componentResponder.getOperation();
    	call[3] = componentResponder.getArgs();
    	call[4] = new InvocationCall();
    	
        RemoteObject ro = serverSession.getRemoteObject();
        AsyncToken token = ro.call("invokeComponent", call, componentResponder);
        
        serverSession.checkWaitForLogout();
        
        return token;
    }
}
