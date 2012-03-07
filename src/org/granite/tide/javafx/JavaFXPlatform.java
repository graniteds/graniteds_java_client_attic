package org.granite.tide.javafx;

import org.granite.tide.EventBus;
import org.granite.tide.data.DataManager;
import org.granite.tide.impl.DummyEventBus;
import org.granite.tide.rpc.ServerSession;

import javafx.application.Platform;


public class JavaFXPlatform implements org.granite.tide.Platform {
	
	private DataManager dataManager = new JavaFXDataManager();
	private ServerSession.Status serverSessionStatus = new JavaFXServerSessionStatus();
	private EventBus eventBus = new DummyEventBus();
	
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession)
			((ServerSession)instance).setStatus(serverSessionStatus);
	}

	@Override
	public DataManager getDataManager() {
		return dataManager;
	}

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	public void execute(Runnable runnable) {
		Platform.runLater(runnable);
	}
}
