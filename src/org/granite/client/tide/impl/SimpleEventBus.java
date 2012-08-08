/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.impl;

import org.granite.client.tide.Context;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;

/**
 * @author William DRAI
 */
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
