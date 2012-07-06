package org.granite.client.tide.events;

import org.granite.client.tide.Context;


public interface TideEvent {
	
	public Context getContext();
	
	public String getType();
	
	public Object[] getArgs();

}
