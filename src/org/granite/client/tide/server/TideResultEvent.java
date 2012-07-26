package org.granite.client.tide.server;

import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideResultEvent<T> extends TideRpcEvent {
    
    private T result;

    public TideResultEvent(Context context, ComponentListener componentResponder, T result) {
        super(context, componentResponder);
        this.result = result;
    }
    
    public T getResult() {
        return result;
    }
    
}
