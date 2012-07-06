package org.granite.client.tide.server;


public interface TideFaultHandler {

    public void call(TideFaultEvent event);
}
