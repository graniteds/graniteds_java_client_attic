package org.granite.client.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

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

	@Override
	public GraniteConfig getGraniteConfig() {
		return graniteConfig;
	}

	@Override
	public ServicesConfig getServicesConfig() {
		return servicesConfig;
	}
}
