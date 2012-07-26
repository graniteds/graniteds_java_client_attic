package org.granite.client.tide.server;

import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideRpcEvent {
    
    private Context context;
    private ComponentListener componentResponder;
    private boolean defaultPrevented = false;

    
    public TideRpcEvent(Context context, ComponentListener componentResponder) {
        this.context = context;
        this.componentResponder = componentResponder;
    }
    
    public Context getContext() {
        return context;
    }
    
    public ComponentListener getComponentResponder() {
        return componentResponder;
    }
    
    public void preventDefault() {
        defaultPrevented = true;
    }
    
    public boolean isDefaultPrevented() {
        return defaultPrevented;
    }
    
}
