package org.granite.client.tide.impl;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.Platform;
import org.granite.client.tide.data.impl.JavaBeanDataManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.server.ServerSession;

public class DefaultPlatform implements Platform {
	
	private DataManager dataManager = new JavaBeanDataManager();
	private EventBus eventBus = new SimpleEventBus();
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession) {
			Configuration configuration = new SimpleConfiguration(null, null);
			
			((ServerSession)instance).setConfiguration(configuration);
		}
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
