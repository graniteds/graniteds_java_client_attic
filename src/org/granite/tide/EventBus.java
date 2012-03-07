package org.granite.tide;



public interface EventBus {

    public void raiseEvent(String name, Object... args);
}
