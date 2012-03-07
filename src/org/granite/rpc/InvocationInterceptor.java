package org.granite.rpc;

import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.rpc.ComponentResponder;


public interface InvocationInterceptor {
    
    public void beforeInvocation(Context ctx, Component component, String operation, Object[] args, ComponentResponder componentResponder);

}
