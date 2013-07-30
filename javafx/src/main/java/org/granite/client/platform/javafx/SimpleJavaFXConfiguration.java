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

import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.validation.InvalidValue;
import org.granite.config.GraniteConfig;

/**
 * @author Franck WOLFF
 */
public class SimpleJavaFXConfiguration extends SimpleConfiguration {

	public SimpleJavaFXConfiguration() {
		super("org/granite/client/platform/javafx/granite-config-javafx.xml", null);
	}

	@Override
	public void postLoad(GraniteConfig graniteConfig) {
		ClientAliasRegistry aliasRegistry = new ClientAliasRegistry();
		aliasRegistry.registerAlias(PersistentSet.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentSet");
		aliasRegistry.registerAlias(PersistentBag.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentBag");
		aliasRegistry.registerAlias(PersistentList.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentList");
		aliasRegistry.registerAlias(PersistentMap.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentMap");
		aliasRegistry.registerAlias(InvalidValue.class.getName(), "org.granite.tide.validators.InvalidValue");
		graniteConfig.setAliasRegistry(aliasRegistry);
	}
}
