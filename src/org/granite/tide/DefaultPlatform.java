package org.granite.tide;

import org.granite.tide.data.DataManager;
import org.granite.tide.data.DefaultDataManager;
import org.granite.tide.impl.SimpleEventBus;

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
