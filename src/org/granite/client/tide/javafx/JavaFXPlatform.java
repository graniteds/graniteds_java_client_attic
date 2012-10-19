/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.javafx;

import javafx.application.Platform;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.persistence.javafx.PersistentBag;
import org.granite.client.persistence.javafx.PersistentList;
import org.granite.client.persistence.javafx.PersistentMap;
import org.granite.client.persistence.javafx.PersistentSet;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleEventBus;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.validation.InvalidValue;
import org.granite.config.GraniteConfig;

/**
 * @author William DRAI
 */
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
			
			Configuration configuration = new SimpleConfiguration("org/granite/client/tide/javafx/granite-config-javafx.xml", null);
			configuration.addConfigurator(new Configuration.Configurator() {
				@Override
				public void configure(GraniteConfig graniteConfig) {
					graniteConfig.registerClassAlias(PersistentSet.class);
					graniteConfig.registerClassAlias(PersistentBag.class);
					graniteConfig.registerClassAlias(PersistentList.class);
					graniteConfig.registerClassAlias(PersistentMap.class);
					graniteConfig.registerClassAlias(InvalidValue.class);
				}
			});
			serverSession.setConfiguration(configuration);
			
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
