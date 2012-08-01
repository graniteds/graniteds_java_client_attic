package org.granite.client.tide.server;

import org.granite.client.tide.Context;


public interface InvocationInterceptor {
    
    public void beforeInvocation(Context ctx, Component component, String operation, Object[] args, ComponentListener componentResponder);

}
