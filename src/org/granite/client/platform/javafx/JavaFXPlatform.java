/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.platform.javafx;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.persistence.javafx.PersistentBag;
import org.granite.client.persistence.javafx.PersistentList;
import org.granite.client.persistence.javafx.PersistentMap;
import org.granite.client.persistence.javafx.PersistentSet;
import org.granite.client.platform.Platform;
import org.granite.client.validation.InvalidValue;
import org.granite.config.GraniteConfig;
import org.granite.messaging.jmf.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class JavaFXPlatform extends Platform {

	public JavaFXPlatform() {
		super(new JavaFXReflection(null));
	}

	public JavaFXPlatform(ClassLoader reflectionClassLoader) {
		super(new JavaFXReflection(reflectionClassLoader));
	}

	public JavaFXPlatform(Reflection reflection) {
		super(reflection);
	}

	@Override
	public Configuration newConfiguration() {
		Configuration configuration = new SimpleConfiguration("org/granite/client/platform/javafx/granite-config-javafx.xml", null);
		configuration.addConfigurator(new Configuration.Configurator() {
			@Override
			public void configure(GraniteConfig graniteConfig) {
				ClientAliasRegistry aliasRegistry = new ClientAliasRegistry();
				aliasRegistry.registerAlias(PersistentSet.class);
				aliasRegistry.registerAlias(PersistentBag.class);
				aliasRegistry.registerAlias(PersistentList.class);
				aliasRegistry.registerAlias(PersistentMap.class);
				aliasRegistry.registerAlias(InvalidValue.class);
				graniteConfig.setAliasRegistry(aliasRegistry);
			}
		});
		return configuration;
	}
}
