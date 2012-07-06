package org.granite.client.tide.server;


public interface TideResultHandler<T> {

    public void call(TideResultEvent<T> event);
}
