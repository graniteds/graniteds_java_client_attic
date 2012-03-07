package org.granite.tide.rpc;


public interface TideFaultHandler {

    public void call(TideFaultEvent event);
}
