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

import java.util.Map;

import org.granite.client.tide.Context;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class JavaFXApplication implements org.granite.client.tide.Application {
    
    private static final Logger log = Logger.getLogger(JavaFXApplication.class);
	
	public void initContext(Context context, Map<String, Object> initialBeans) {
	    DataManager dataManager = new JavaFXDataManager();
	    context.setDataManager(dataManager);
	    try {
	        initialBeans.put("validationManager", new JavaFXValidationManager());
	        initialBeans.put("traversableResolver", new JavaFXTraversableResolver(dataManager));
	    }
	    catch (Exception e) {
	        // Assume Bean Validation not available
	        log.info("Bean validation not available, support not configured");
	    }
	}
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession)
			((ServerSession)instance).setStatus(new JavaFXServerSessionStatus());
	}
	
	@Override
    public void execute(Runnable runnable) {
        javafx.application.Platform.runLater(runnable);
    }
}
