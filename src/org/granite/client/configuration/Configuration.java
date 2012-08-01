package org.granite.client.configuration;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

public interface Configuration {

	GraniteConfig getGraniteConfig();
	ServicesConfig getServicesConfig();
	
	void load();
	
	void addConfigurator(Configurator configurator);
	
	static interface Configurator {		
		void configure(GraniteConfig graniteConfig);
	}
}
