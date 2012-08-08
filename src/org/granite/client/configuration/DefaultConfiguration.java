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

package org.granite.client.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * @author Franck WOLFF
 */
public class DefaultConfiguration implements Configuration {

	private static final String DEFAULT_CONFIG_PATH = DefaultConfiguration.class.getPackage().getName().replace('.', '/') + "/granite-config.xml";
	
	private static Configuration instance = null;

	public static synchronized Configuration getInstance() {
		if (instance == null)
			instance = new DefaultConfiguration();
		return instance;
	}
	
	private GraniteConfig graniteConfig;
	private ServicesConfig servicesConfig;

	public DefaultConfiguration() {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_PATH);
			graniteConfig = new GraniteConfig(null, is, null, null);
			servicesConfig = new ServicesConfig(null, null, false);
		}
		catch (Exception e) {
			graniteConfig = null;
			servicesConfig = null;
			throw new RuntimeException("Could not load configuration", e);
		}
		finally {
			if (is != null) try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	public void addConfigurator(Configurator configurator) {		
	}
	
	public void load() {		
	}

	@Override
	public GraniteConfig getGraniteConfig() {
		return graniteConfig;
	}

	@Override
	public ServicesConfig getServicesConfig() {
		return servicesConfig;
	}
}
