package org.granite.tide.events;

import org.granite.tide.Context;


public interface TideEvent {
	
	public Context getContext();
	
	public String getType();
	
	public Object[] getArgs();

}
