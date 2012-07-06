package org.granite.client.tide.server;

import java.util.Map;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideFaultEvent extends TideRpcEvent {
    
    private Fault fault;
    private Map<String, Object> extendedData;

    public TideFaultEvent(Context context, RequestMessage request, ComponentResponder componentResponder, Fault fault, Map<String, Object> extendedData) {
        super(context, request, componentResponder);
        this.fault = fault;
        this.extendedData = extendedData;
    }

    public Fault getFault() {
        return fault;
    }
    
    public void setFault(Fault fault) {
        this.fault = fault;
    }
    
    public Map<String, Object> getExtendedData() {
        return extendedData;
    }
    
    public void setExtendedData(Map<String, Object> extendedData) {
        this.extendedData = extendedData;
    }

}
