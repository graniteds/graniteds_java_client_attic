package org.granite.client.tide.impl;

import org.granite.client.tide.EventBus;
import org.granite.client.tide.Platform;
import org.granite.client.tide.data.impl.DefaultDataManager;
import org.granite.client.tide.data.spi.DataManager;

public class DefaultPlatform implements Platform {
	
	private DataManager dataManager = new DefaultDataManager();
	private EventBus eventBus = new SimpleEventBus();
	
	public void configure(Object instance) {		
	}

	@Override
	public void execute(Runnable runnable) {
		runnable.run();
	}

	@Override
	public DataManager getDataManager() {
		return dataManager;
	}

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

}
