package org.granite.client.configuration;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

public interface Configuration {

	GraniteConfig getGraniteConfig();
	ServicesConfig getServicesConfig();
}
