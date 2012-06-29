package org.granite.tide.server;



public interface TideResponder<T> {
    
    public void result(TideResultEvent<T> event);
    
    public void fault(TideFaultEvent event);

}
