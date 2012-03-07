package org.granite.tide.impl;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;
import org.granite.rpc.AsyncToken;
import org.granite.rpc.remoting.RemoteObject;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.ContextAware;
import org.granite.tide.NameAware;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.rpc.ComponentResponder;
import org.granite.tide.rpc.ServerSession;


public class ComponentImpl implements Component, ContextAware, NameAware {
    
    @SuppressWarnings("unused")
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
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        
        return (Future<T>)context.callComponent(serverSession, this, operation, args, false);
    }
    
    
    public AsyncToken invoke(ComponentResponder componentResponder) {
        AsyncToken token = null;
        RemoteObject ro = getServerSession().getRemoteObject();
        if (ro != null) {
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
            token = ro.call("invokeComponent", call, componentResponder);
        }
        else
        	throw new RuntimeException("Internal RemoteObject not created");
        
        getServerSession().checkWaitForLogout();            
        
        return token;
    }
}
