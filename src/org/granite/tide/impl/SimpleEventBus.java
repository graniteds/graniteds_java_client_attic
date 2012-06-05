package org.granite.tide.impl;

import org.granite.tide.Context;
import org.granite.tide.EventBus;
import org.granite.tide.events.TideEvent;
import org.granite.tide.events.TideEventObserver;


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
