package org.granite.tide.impl;

import org.granite.tide.EventBus;


public class DummyEventBus implements EventBus {

    @Override
    public void raiseEvent(String name, Object... args) {
        // Do nothing        
    }

}
