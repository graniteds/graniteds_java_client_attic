package org.granite.tide.server;

import org.granite.tide.Context;


public interface InvocationInterceptor {
    
    public void beforeInvocation(Context ctx, Component component, String operation, Object[] args, ComponentResponder componentResponder);

}
