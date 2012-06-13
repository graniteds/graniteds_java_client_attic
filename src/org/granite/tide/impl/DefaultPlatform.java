package org.granite.tide.impl;

import org.granite.tide.EventBus;
import org.granite.tide.Platform;
import org.granite.tide.data.impl.DefaultDataManager;
import org.granite.tide.data.spi.DataManager;

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
