package org.granite.client.tide.server;



public interface TideResponder<T> {
    
    public void result(TideResultEvent<T> event);
    
    public void fault(TideFaultEvent event);

}
