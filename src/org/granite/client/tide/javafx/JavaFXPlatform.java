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

import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.server.ServerSession;

/**
 * @author William DRAI
 */
public class JavaFXPlatform implements org.granite.client.tide.Platform {
	
	private DataManager dataManager = new JavaFXDataManager();
	private final ServerSession.Status serverSessionStatus = new JavaFXServerSessionStatus();
	
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession) {
			ServerSession serverSession = (ServerSession)instance;
			serverSession.setStatus(serverSessionStatus);
		}
	}
	
	@Override
	public DataManager getDataManager() {
		return dataManager;
	}

	public void execute(Runnable runnable) {
		Platform.runLater(runnable);
	}
}
