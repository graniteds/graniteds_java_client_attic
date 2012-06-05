package org.granite.tide.rpc;

import org.granite.rpc.AsyncToken;
import org.granite.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideRpcEvent {
    
    private Context context;
    private AsyncToken token;
    private ComponentResponder componentResponder;
    private boolean defaultPrevented = false;

    public TideRpcEvent(Context context,
            AsyncToken token, ComponentResponder componentResponder) {
        this.context = context;
        this.token = token;
        this.componentResponder = componentResponder;
    }
    
    public Context getContext() {
        return context;
    }
    
    public AsyncToken getToken() {
        return token;
    }
    
    public ComponentResponder getComponentResponder() {
        return componentResponder;
    }
    
    public void preventDefault() {
        defaultPrevented = true;
    }
    
    public boolean isDefaultPrevented() {
        return defaultPrevented;
    }
    
}
