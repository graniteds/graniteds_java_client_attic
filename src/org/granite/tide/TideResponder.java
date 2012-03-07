package org.granite.tide;

import org.granite.tide.rpc.TideFaultEvent;
import org.granite.tide.rpc.TideResultEvent;


public interface TideResponder<T> {
    
    public void result(TideResultEvent<T> event);
    
    public void fault(TideFaultEvent event);

}
