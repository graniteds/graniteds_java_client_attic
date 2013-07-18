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

package org.granite.client.messaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.granite.logging.Logger;
import org.granite.messaging.AliasRegistry;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;


/**
 * @author William DRAI
 */
public class ClientAliasRegistry implements AliasRegistry {
	
	private static final Logger log = Logger.getLogger(ClientAliasRegistry.class);
	
    private static final String MESSAGING_SCAN_MARKER = "META-INF/messaging-scan.properties";
	
	private Map<String, String> serverToClientAliases = new HashMap<String, String>();
	private Map<String, String> clientToServerAliases = new HashMap<String, String>();
	private String scanMarker;
	

	public String getScanMarker() {
		return scanMarker;
	}

	public void setScanMarker(String scanMarker) {
		this.scanMarker = scanMarker;
	}
	
	
	public void scan(Set<String> packageNames) {
		if (!packageNames.isEmpty()) {
			try {
				RemoteAliasScanner.scan(this, packageNames);
			}
			catch (Exception e) {
				log.debug("Extcos scanner not available, using classpath scanner");
			}
		}
		
		scanMarker = (scanMarker != null ? scanMarker : MESSAGING_SCAN_MARKER);
		Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(), scanMarker);
        try {
            scanner.scan();
        }
        catch (Exception e) {
            log.error(e, "Could not scan classpath for @RemoteAlias");
        }
	}
	
	public void registerAlias(Class<?> remoteAliasAnnotatedClass) {
		RemoteAlias remoteAlias = remoteAliasAnnotatedClass.getAnnotation(RemoteAlias.class);
		if (remoteAlias == null)
			throw new IllegalArgumentException(remoteAliasAnnotatedClass.getName() + " isn't annotated with " + RemoteAlias.class.getName());
		registerAlias(remoteAliasAnnotatedClass.getName(), remoteAlias.value());
	}

	public void registerAliases(Class<?>... remoteAliasAnnotatedClasses) {
		for (Class<?> remoteAliasAnnotatedClass : remoteAliasAnnotatedClasses)
			registerAlias(remoteAliasAnnotatedClass);
	}

	public void registerAlias(String clientClassName, String serverClassName) {
		if (clientClassName.length() == 0 || serverClassName.length() == 0)
			throw new IllegalArgumentException("Empty class name: " + clientClassName + " / " + serverClassName);
		
		clientToServerAliases.put(clientClassName, serverClassName);
		serverToClientAliases.put(serverClassName, clientClassName);
	}

	public void registerAliases(Map<String, String> clientToServerAliases) {
		for (Map.Entry<String, String> clientToServerAlias : clientToServerAliases.entrySet())
			registerAlias(clientToServerAlias.getKey(), clientToServerAlias.getValue());
	}

	public String getAliasForType(String className) {
		String alias = clientToServerAliases.get(className);
		return (alias != null ? alias : className);
	}
	
	public String getTypeForAlias(String alias) {
		String className = serverToClientAliases.get(alias);
		return className != null ? className : alias;
	}
	
	
	class MessagingScannedItemHandler implements ScannedItemHandler {

		@Override
		public boolean handleMarkerItem(ScannedItem item) {
			return false;
		}

		@Override
		public void handleScannedItem(ScannedItem item) {
			if ("class".equals(item.getExtension())) {
				try {
					Class<?> cls = item.loadAsClass();
					RemoteAlias alias = cls.getAnnotation(RemoteAlias.class);
					if (alias != null)
						registerAlias(cls);
				}
				catch (ClassFormatError e) {
				}
				catch (ClassNotFoundException e) {
				}
				catch (IOException e) {
					log.error(e, "Could not load class: %s", item);
				}
			}
		}
	}
	
}
