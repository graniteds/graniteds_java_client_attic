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
import java.util.ArrayList;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * @author Franck WOLFF
 */
public class SimpleConfiguration implements Configuration {

	private String graniteStdConfigPath = null;
	private String graniteConfigPath = null;
	private List<Configurator> configurators = new ArrayList<Configurator>(); 
	
	private GraniteConfig graniteConfig = null;
	private ServicesConfig servicesConfig = null;

	
	public SimpleConfiguration() {		
	}
	
	public SimpleConfiguration(String graniteStdConfigPath, String graniteConfigPath) {
		this.graniteStdConfigPath = graniteStdConfigPath;
		this.graniteConfigPath = graniteConfigPath;
	}
	
	public void setGraniteStdConfigPath(String graniteConfigPath) {
		this.graniteStdConfigPath = graniteConfigPath;
	}
	
	public void setGraniteConfigPath(String graniteConfigPath) {
		this.graniteConfigPath = graniteConfigPath;
	}
	
	public void addConfigurator(Configurator configurator) {
		this.configurators.add(configurator);
	}
	
	public void load() {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/granite/client/configuration/granite-config.xml");
			if (graniteConfigPath != null)
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(graniteConfigPath);
			graniteConfig = new GraniteConfig(graniteStdConfigPath, is, null, null);
			for (Configurator configurator : configurators)
				configurator.configure(graniteConfig);
			servicesConfig = new ServicesConfig(null, null, false);
		}
		catch (Exception e) {
			graniteConfig = null;
			servicesConfig = null;
			throw new RuntimeException("Cannot load configuration", e);
		}
		finally {
			if (is != null) try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	public GraniteConfig getGraniteConfig() {
		return graniteConfig;
	}
	
	public ServicesConfig getServicesConfig() {
		return servicesConfig;
	}

}
