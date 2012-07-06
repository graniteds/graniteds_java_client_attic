package org.granite.client.tide;

import org.granite.client.tide.data.spi.DataManager;


public interface Platform {

	public DataManager getDataManager();

	public EventBus getEventBus();
	
	public void configure(Object instance);

	public void execute(Runnable runnable);
}
