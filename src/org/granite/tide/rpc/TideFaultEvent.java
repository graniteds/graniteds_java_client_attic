package org.granite.tide.rpc;

import java.util.Map;

import org.granite.rpc.AsyncToken;
import org.granite.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideFaultEvent extends TideRpcEvent {
    
    private Fault fault;
    private Map<String, Object> extendedData;

    public TideFaultEvent(Context context,
            AsyncToken token, ComponentResponder componentResponder, Fault fault, Map<String, Object> extendedData) {
        super(context, token, componentResponder);
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
