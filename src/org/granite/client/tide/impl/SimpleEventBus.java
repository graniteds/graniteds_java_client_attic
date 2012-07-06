package org.granite.client.tide.impl;

import org.granite.client.tide.Context;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;


public class SimpleEventBus implements EventBus {
	
    @Override
    public void raiseEvent(Context context, String type, Object... args) {
    	SimpleTideEvent event = new SimpleTideEvent(context, type, args);
    	
    	raiseEvent(event);
    }
    
    protected void raiseEvent(TideEvent event) {
    	TideEventObserver[] observers = event.getContext().allByType(TideEventObserver.class);
    	for (TideEventObserver observer : observers)
    		observer.handleEvent(event);
    }

}
