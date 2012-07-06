package org.granite.client.tide.javafx;

import javafx.application.Platform;

import org.granite.client.tide.EventBus;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleEventBus;
import org.granite.client.tide.server.ServerSession;


public class JavaFXPlatform implements org.granite.client.tide.Platform {
	
	private DataManager dataManager = new JavaFXDataManager();
	private final ServerSession.Status serverSessionStatus = new JavaFXServerSessionStatus();
	private final EventBus eventBus;
	
	
	public JavaFXPlatform() {
		eventBus = new SimpleEventBus();
	}
	
	public JavaFXPlatform(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession) {
			ServerSession serverSession = (ServerSession)instance;
			
//			String graniteStdConfigPath = "org/granite/client/tide/javafx/granite-config-javafx.xml";
//			Engine.Configurator graniteConfigurator = new Engine.Configurator() {
//				@Override
//				public void configure(GraniteConfig graniteConfig) {
//					graniteConfig.registerClassAlias(PersistentSet.class);
//					graniteConfig.registerClassAlias(PersistentBag.class);
//					graniteConfig.registerClassAlias(PersistentList.class);
//					graniteConfig.registerClassAlias(PersistentMap.class);
//				}
//			};
//
//			if (serverSession.getHttpClientEngine() == null)
//				serverSession.setHttpClientEngine(new ApacheAsyncTransport());
//			serverSession.getHttpClientEngine().setGraniteStdConfigPath(graniteStdConfigPath);
//			serverSession.getHttpClientEngine().setGraniteConfigurator(graniteConfigurator);

//			if (serverSession.getWebSocketEngine() == null)
//				serverSession.setWebSocketEngine(new JettyWebSocketEngine());
//			serverSession.getWebSocketEngine().setGraniteStdConfigPath(graniteStdConfigPath);
//			serverSession.getWebSocketEngine().setGraniteConfigurator(graniteConfigurator);
			
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
