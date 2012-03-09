package org.granite.tide.javafx;

import javafx.application.Platform;

import org.granite.config.GraniteConfig;
import org.granite.messaging.engine.Engine;
import org.granite.tide.EventBus;
import org.granite.tide.data.DataManager;
import org.granite.tide.impl.DummyEventBus;
import org.granite.tide.rpc.ServerSession;


public class JavaFXPlatform implements org.granite.tide.Platform {
	
	private DataManager dataManager = new JavaFXDataManager();
	private ServerSession.Status serverSessionStatus = new JavaFXServerSessionStatus();
	private EventBus eventBus = new DummyEventBus();
	
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession) {
			ServerSession serverSession = (ServerSession)instance;

			serverSession.getEngine().setGraniteStdConfigPath("org/granite/tide/javafx/granite-config-javafx.xml");
			serverSession.getEngine().setGraniteConfigurator(new Engine.Configurator() {
				@Override
				public void configure(GraniteConfig graniteConfig) {
					graniteConfig.registerClassAlias(PersistentSet.class);
					graniteConfig.registerClassAlias(PersistentBag.class);
					graniteConfig.registerClassAlias(PersistentList.class);
					graniteConfig.registerClassAlias(PersistentMap.class);
				}
			});
			
			serverSession.setStatus(serverSessionStatus);
		}
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
