package org.granite.client.tide;


public interface EventBus {

    public void raiseEvent(Context context, String type, Object... args);
}
