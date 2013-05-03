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

package org.granite.client.messaging.jmf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;

/**
 * @author Franck WOLFF
 */
public class DefaultClientSharedContext extends DefaultSharedContext implements ClientSharedContext {

	protected final Map<String, String> classNameAliases;
	
	public DefaultClientSharedContext() {
		this(null, null, null);
	}

	public DefaultClientSharedContext(CodecRegistry codecRegistry) {
		this(codecRegistry, null, null);
	}

	public DefaultClientSharedContext(CodecRegistry codecRegistry, ClassLoader classLoader) {
		this(codecRegistry, classLoader, null);
	}

	public DefaultClientSharedContext(CodecRegistry codecRegistry, ClassLoader classLoader, List<String> defaultStoredStrings) {
		super(codecRegistry, classLoader, defaultStoredStrings);
		
		this.classNameAliases = new HashMap<String, String>();
	}
	
	public void registerAlias(String clientClassName, String serverClassName) {
		classNameAliases.put(clientClassName, serverClassName);
		classNameAliases.put(serverClassName, clientClassName);
	}

	@Override
	public String getAlias(String className) {
		String alias = classNameAliases.get(className);
		return (alias != null ? alias : className);
	}
}
