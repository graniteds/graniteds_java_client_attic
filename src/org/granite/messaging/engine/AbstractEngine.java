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
import java.io.OutputStream;
import java.util.HashMap;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractEngine implements Engine {

	protected static final String CONTENT_TYPE = "application/x-amf";

	protected boolean started = false;
	protected GraniteConfig graniteConfig = null;
	protected Configurator configurator = null;
	protected ServicesConfig servicesConfig = null;
	protected EngineStatusHandler statusHandler = new DefaultEngineStatusHandler();
	protected String graniteStdConfigPath = "org/granite/messaging/engine/granite-config.xml";
	protected String graniteConfigPath = null;
	

	public EngineStatusHandler getStatusHandler() {
		return statusHandler;
	}

	public void setStatusHandler(EngineStatusHandler statusHandler) {
		if (statusHandler == null)
			throw new NullPointerException("statusHandler cannot be null");
		this.statusHandler = statusHandler;
	}
	
	public void setGraniteStdConfigPath(String graniteConfigPath) {
		this.graniteStdConfigPath = graniteConfigPath;
	}
	
	public void setGraniteConfigPath(String graniteConfigPath) {
		this.graniteConfigPath = graniteConfigPath;
	}
	
	public void setGraniteConfigurator(Configurator configurator) {
		this.configurator = configurator;
	}
	
	@Override
	public void start() {

		if (started) {
			statusHandler.handleException(new EngineException("Engine already started"));
			return;
		}

		InputStream is = null;
		try {
			if (graniteConfigPath != null)
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(graniteConfigPath);
			graniteConfig = new GraniteConfig(graniteStdConfigPath, null, null, null);
			if (configurator != null)
				configurator.configure(graniteConfig);
			servicesConfig = new ServicesConfig(null, null, false);
			started = true;
		}
		catch (Exception e) {
			graniteConfig = null;
			servicesConfig = null;
			statusHandler.handleException(new EngineException("Could not load default configuration", e));
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
	
	protected void serialize(AMF0Message message, OutputStream os) throws IOException {
		AMF0Serializer serializer = new AMF0Serializer(os);
		SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>(0));
		try {
			serializer.serializeMessage(message);
		}
		finally {
			GraniteContext.release();
		}
	}
	
	protected AMF0Message deserialize(InputStream is) throws IOException {
		SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>(0));
		try {
			AMF0Deserializer deserializer = new AMF0Deserializer(is);
			return deserializer.getAMFMessage();
		}
		finally {
			GraniteContext.release();
		}
	}

	@Override
	public void stop() {
		if (!started)
			statusHandler.handleException(new EngineException("Engine not started"));
		else {
			graniteConfig = null;
			servicesConfig = null;
			started = false;
		}
	}
}
