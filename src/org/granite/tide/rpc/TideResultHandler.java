package org.granite.tide.rpc;


public interface TideResultHandler<T> {

    public void call(TideResultEvent<T> event);
}
