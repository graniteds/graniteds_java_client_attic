package org.granite.client.tide.server;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideRpcEvent {
    
    private Context context;
    private RequestMessage request;
    private ComponentResponder componentResponder;
    private boolean defaultPrevented = false;

    public TideRpcEvent(Context context, RequestMessage request, ComponentResponder componentResponder) {
        this.context = context;
        this.request = request;
        this.componentResponder = componentResponder;
    }
    
    public Context getContext() {
        return context;
    }
    
    public RequestMessage getToken() {
        return request;
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
