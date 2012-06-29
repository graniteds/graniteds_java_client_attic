package org.granite.tide.server;



public interface TideMergeResponder<T> extends TideResponder<T> {
    
    public T getMergeResultWith();

}
