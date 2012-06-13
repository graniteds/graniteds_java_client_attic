package org.granite.tide.server;


public interface TideFaultHandler {

    public void call(TideFaultEvent event);
}
