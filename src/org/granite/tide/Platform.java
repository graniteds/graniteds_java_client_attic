package org.granite.tide;

import org.granite.tide.data.DataManager;


public interface Platform {

	public DataManager getDataManager();

	public EventBus getEventBus();
	
	public void configure(Object instance);

	public void execute(Runnable runnable);
}
