package org.granite.tide;

import org.granite.tide.server.TideFaultEvent;
import org.granite.tide.server.TideResultEvent;


public interface TideResponder<T> {
    
    public void result(TideResultEvent<T> event);
    
    public void fault(TideFaultEvent event);

}
