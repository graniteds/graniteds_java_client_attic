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
import java.util.Map;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.logging.Logger;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class AMFChannelFactory extends AbstractChannelFactory {
    
	private static final Logger log = Logger.getLogger(AMFChannelFactory.class);
	
	private final Configuration defaultConfiguration;
	
	public AMFChannelFactory() {
		this(null, null, null);
	}
	
	public AMFChannelFactory(Configuration defaultConfiguration) {
		this(null, null, defaultConfiguration);
	}

	public AMFChannelFactory(Transport remotingTransport, Transport messagingTransport) {
		this(remotingTransport, messagingTransport, null);
	}

	public AMFChannelFactory(Transport remotingTransport, Transport messagingTransport, Configuration defaultConfiguration) {
		super(ContentType.AMF, remotingTransport, messagingTransport);
		
		this.defaultConfiguration = (defaultConfiguration != null ? defaultConfiguration : Platform.getInstance().newConfiguration());
	}

	@Override
	public AMFRemotingChannel newRemotingChannel(String id, URI uri) {
		return newRemotingChannel(id, uri, RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
	}

	@Override
	public AMFRemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests) {
		return new AMFRemotingChannel(getRemotingTransport(), defaultConfiguration, id, uri, maxConcurrentRequests);
	}

	@Override
	public AMFMessagingChannel newMessagingChannel(String id, URI uri) {
		return new AMFMessagingChannel(getMessagingTransport(), defaultConfiguration, id, uri);
	}
	
	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
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
