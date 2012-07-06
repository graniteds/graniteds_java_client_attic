package org.granite.client.tide.server;



public interface TideMergeResponder<T> extends TideResponder<T> {
    
    public T getMergeResultWith();

}
