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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;
import org.granite.util.JMFAMFUtil;

/**
 * @author Franck WOLFF
 */
public class ClientSharedContextFactory {

    private static final Logger log = Logger.getLogger(ClientSharedContextFactory.class);
	
    private static final String MESSAGING_SCAN_MARKER = "META-INF/messaging-scan.properties";
	
	private static ClientSharedContext context = null;
	
	public static synchronized void initialize() {
		initialize(MESSAGING_SCAN_MARKER);
	}
	
	public static synchronized void initialize(String marker) {
		Map<String, String> clientToServerAliases = new HashMap<String, String>();
		Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(clientToServerAliases), marker);
        try {
            scanner.scan();
        } catch (Exception e) {
            log.error(e, "Could not scan classpath for configuration");
        }
        
        List<ExtendedObjectCodec> extendedCodecs = new ArrayList<ExtendedObjectCodec>();
        extendedCodecs.add(new ClientEntityCodec());
 		initialize(extendedCodecs, clientToServerAliases);
	}
	
	public static synchronized void initialize(List<ExtendedObjectCodec> extendedCodecs, Map<String, String> clientToServerAliases) {
		context = new DefaultClientSharedContext(new DefaultCodecRegistry(extendedCodecs), JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS, null);
		
		if (clientToServerAliases != null) {
			for (Map.Entry<String, String> clientToServerAlias : clientToServerAliases.entrySet())
				context.registerAlias(clientToServerAlias.getKey(), clientToServerAlias.getValue());
		}
	}
	
	public static synchronized ClientSharedContext getInstance() {
		if (context == null)
			context = new DefaultClientSharedContext(new DefaultCodecRegistry(), JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS, null);
		return context;
	}
	
	static class MessagingScannedItemHandler implements ScannedItemHandler {

		final Map<String, String> clientToServerAliases;
		
		MessagingScannedItemHandler(Map<String, String> clientToServerAliases) {
			this.clientToServerAliases = clientToServerAliases;
		}
		
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
						clientToServerAliases.put(cls.getName(), alias.value());
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
