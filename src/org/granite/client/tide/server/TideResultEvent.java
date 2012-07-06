package org.granite.client.tide.server;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideResultEvent<T> extends TideRpcEvent {
    
    private T result;

    public TideResultEvent(Context context, RequestMessage request, ComponentResponder componentResponder, T result) {
        super(context, request, componentResponder);
        this.result = result;
    }
    
    public T getResult() {
        return result;
    }
}
