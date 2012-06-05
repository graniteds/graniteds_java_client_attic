/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.engine;

import java.io.IOException;
import java.io.InputStream;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractEngine implements Engine {

	protected static final String CONTENT_TYPE = "application/x-amf";

	protected boolean started = false;
	protected GraniteConfig graniteConfig = null;
	protected ServicesConfig servicesConfig = null;
<<<<<<< HEAD
	protected EngineExceptionHandler exceptionHandler = new LogEngineExceptionHandler();
=======
	protected EngineStatusHandler statusHandler = new DefaultEngineStatusHandler();
	protected String graniteStdConfigPath = "org/granite/messaging/engine/granite-config.xml";
	protected String graniteConfigPath = null;
	protected int maxIdleTime = 30000;
>>>>>>> 6e5c8c0... Tide implementation for Java client
	
	public EngineExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(EngineExceptionHandler exceptionHandler) {
		if (exceptionHandler == null)
			throw new NullPointerException("ExceptionHandler cannot be null");
		this.exceptionHandler = exceptionHandler;
	}
<<<<<<< HEAD

=======
	
	public void setGraniteConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
	
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
>>>>>>> 6e5c8c0... Tide implementation for Java client
	@Override
	public void start() {

		if (started) {
			exceptionHandler.handle(new EngineException("Engine already started"));
			return;
		}

		InputStream is = null;
		try {
<<<<<<< HEAD
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/messaging/engine/granite-config.xml");
			graniteConfig = new GraniteConfig(null, is, null, null);
=======
			if (graniteConfigPath != null)
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(graniteConfigPath);
			graniteConfig = new GraniteConfig(graniteStdConfigPath, is, null, null);
			if (configurator != null)
				configurator.configure(graniteConfig);
>>>>>>> 6e5c8c0... Tide implementation for Java client
			servicesConfig = new ServicesConfig(null, null, false);
			started = true;
		}
		catch (Exception e) {
			graniteConfig = null;
			servicesConfig = null;
			exceptionHandler.handle(new EngineException("Could not load default configuration", e));
		}
		finally {
			if (is != null) try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	public boolean isStarted() {
		return started;
	}

	@Override
	public void stop() {
		if (!started)
			exceptionHandler.handle(new EngineException("Engine not started"));
		else {
			graniteConfig = null;
			servicesConfig = null;
			started = false;
		}
	}
}
