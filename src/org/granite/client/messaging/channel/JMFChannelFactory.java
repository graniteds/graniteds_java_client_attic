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

package org.granite.client.messaging.channel;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.channel.amf.JMFAMFMessagingChannel;
import org.granite.client.messaging.channel.amf.JMFAMFRemotingChannel;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.reflect.Reflection;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;
import org.granite.util.ContentType;
import org.granite.util.JMFAMFUtil;

/**
 * @author Franck WOLFF
 */
public class JMFChannelFactory extends AbstractChannelFactory {
    
	private static final Logger log = Logger.getLogger(JMFChannelFactory.class);
	
    private static final String MESSAGING_SCAN_MARKER = "META-INF/messaging-scan.properties";

	private ClientSharedContext sharedContext = null;
	
	private List<ExtendedObjectCodec> extendedCodecs = null;
	private List<String> defaultStoredStrings = null;
	private Map<String, String> clientToServerAliases = null;
	private Reflection reflection = null;
	private boolean scanRemoteAliases = true;
	private String scanMarker = null;
	
	public JMFChannelFactory() {
		super(ContentType.JMF_AMF);
	}

	public JMFChannelFactory(ClientSharedContext sharedContext, Configuration configuration, Transport remotingTransport, Transport messagingTransport) {
		super(ContentType.JMF_AMF, configuration, remotingTransport, messagingTransport);
		
		this.sharedContext = sharedContext;
	}

	public ClientSharedContext getSharedContext() {
		return sharedContext;
	}

	public void setSharedContext(ClientSharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}
	
	public List<ExtendedObjectCodec> getExtendedCodecs() {
		return extendedCodecs;
	}

	public void setExtendedCodecs(List<ExtendedObjectCodec> extendedCodecs) {
		this.extendedCodecs = extendedCodecs;
	}

	public List<String> getDefaultStoredStrings() {
		return defaultStoredStrings;
	}

	public void setDefaultStoredStrings(List<String> defaultStoredStrings) {
		this.defaultStoredStrings = defaultStoredStrings;
	}

	public Map<String, String> getClientToServerAliases() {
		return clientToServerAliases;
	}

	public void setClientToServerAliases(Map<String, String> clientToServerAliases) {
		this.clientToServerAliases = clientToServerAliases;
	}

	public Reflection getReflection() {
		return reflection;
	}

	public void setReflection(Reflection reflection) {
		this.reflection = reflection;
	}

	public boolean getScanRemoteAliases() {
		return scanRemoteAliases;
	}

	public void setScanRemoteAliases(boolean scanRemoteAliases) {
		this.scanRemoteAliases = scanRemoteAliases;
	}

	public String getScanMarker() {
		return scanMarker;
	}

	public void setScanMarker(String scanMarker) {
		this.scanMarker = scanMarker;
	}

	@Override
	public void start() {
		super.start();
		
		if (sharedContext == null) {
			
			extendedCodecs = (extendedCodecs != null ? extendedCodecs : new ArrayList<ExtendedObjectCodec>(Arrays.asList(new ClientEntityCodec())));
			defaultStoredStrings = (defaultStoredStrings != null ? defaultStoredStrings : new ArrayList<String>(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS));
			clientToServerAliases = (clientToServerAliases != null ? clientToServerAliases : new HashMap<String, String>());
			reflection = (reflection != null ? reflection : Platform.reflection());
			
			if (scanRemoteAliases) {
				Map<String, String> scannedAliases = new HashMap<String, String>();
				
				scanMarker = (scanMarker != null ? scanMarker : MESSAGING_SCAN_MARKER);
				Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(scannedAliases), scanMarker);
		        try {
		            scanner.scan();
		        }
		        catch (Exception e) {
		            log.error(e, "Could not scan classpath for @RemoteAlias");
		        }

		        scannedAliases.putAll(clientToServerAliases);
		        clientToServerAliases = scannedAliases;
			}
			
			sharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(extendedCodecs), defaultStoredStrings, reflection);
			sharedContext.registerAliases(clientToServerAliases);
		}
	}

	@Override
	public void stop(boolean stopTransports) {
		try {
			super.stop(stopTransports);
		}
		finally {
			sharedContext = null;
			
			extendedCodecs = null;
			defaultStoredStrings = null;
			clientToServerAliases = null;
			reflection = null;
			scanRemoteAliases = true;
			scanMarker = null;
		}
	}

	@Override
	public JMFAMFRemotingChannel newRemotingChannel(String id, URI uri) {
		return new JMFAMFRemotingChannel(remotingTransport, id, uri, sharedContext);
	}

	@Override
	public JMFAMFRemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests) {
		return new JMFAMFRemotingChannel(remotingTransport, id, uri, sharedContext, maxConcurrentRequests);
	}

	@Override
	public JMFAMFMessagingChannel newMessagingChannel(String id, URI uri) {
		return new JMFAMFMessagingChannel(messagingTransport, id, uri, sharedContext);
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
