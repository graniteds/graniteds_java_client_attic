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

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.channel.amf.JMFAMFMessagingChannel;
import org.granite.client.messaging.channel.amf.JMFAMFRemotingChannel;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.ClientSharedContextFactory;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class ChannelFactory {

	public static final String SYSTEM_PROPERTY_KEY = ChannelFactory.class.getName();

	protected static final ServiceLoader<ChannelFactory> channelFactoryLoader = ServiceLoader.load(ChannelFactory.class);
	
	protected ContentType contentType = null;
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;
	protected ClientSharedContext sharedContext = null;

	public static synchronized ChannelFactory newInstance() {
		String factoryClassName = System.getProperty(SYSTEM_PROPERTY_KEY);
		
		if (factoryClassName == null) {
			Iterator<ChannelFactory> it = channelFactoryLoader.iterator();
			if (it.hasNext()) {
				ChannelFactory factory = it.next();
				return factory;
			}
		}
		
		if (factoryClassName == null)
			factoryClassName = ChannelFactory.class.getName();

		return newInstance(factoryClassName, ChannelFactory.class.getClassLoader());
	}
	
	public static synchronized ChannelFactory newInstance(String factoryClassName, ClassLoader classLoader)
		throws ChannelFactoryConfigurationError {
		
		if (factoryClassName == null)
			throw new NullPointerException("factoryClassName cannot be null");
		
		if (classLoader == null)
			classLoader = ChannelFactory.class.getClassLoader();
		
		ChannelFactory factory = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ChannelFactory> factoryClass = (Class<? extends ChannelFactory>)classLoader.loadClass(factoryClassName);
			factory = factoryClass.getConstructor().newInstance();
		}
		catch (Throwable t) {
			throw new ChannelFactoryConfigurationError("Could not create new ChannelFactory: " + factoryClassName, t);
		}
		
		return factory;
	}
	
	public ChannelFactory() {
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public Transport getRemotingTransport() {
		return remotingTransport;
	}

	public void setRemotingTransport(Transport remotingTransport) {
		this.remotingTransport = remotingTransport;
	}

	public Transport getMessagingTransport() {
		return messagingTransport;
	}

	public void setMessagingTransport(Transport messagingTransport) {
		this.messagingTransport = messagingTransport;
	}
	
	public ClientSharedContext getSharedContext() {
		return sharedContext;
	}

	public void setSharedContext(ClientSharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}

	protected void startTransports() throws TransportException {
		if (remotingTransport == null)
			remotingTransport = new ApacheAsyncTransport();
		
		if (!remotingTransport.isStarted() && !remotingTransport.start())
			throw new TransportException("Could not start remoting transport: " + remotingTransport);
		
		if (messagingTransport == null)
			messagingTransport = remotingTransport;
		else if (!messagingTransport.isStarted() && !messagingTransport.start())
			throw new TransportException("Could not start messaging transport: " + messagingTransport);
	}
	
	protected void stopTransports() throws TransportException {
		if (remotingTransport != null && remotingTransport.isStarted()) {
			remotingTransport.stop();
			remotingTransport = null;
		}
		
		if (messagingTransport != null && messagingTransport.isStarted()) {
			messagingTransport.stop();
			messagingTransport = null;
		}
	}
	
	protected void startMessagingProtocol() {
		if (contentType == ContentType.JMF_AMF)
			ClientSharedContextFactory.initialize();
	}
	
	protected void stopMessagingProtocol() {
	}
	
	public void start() throws TransportException {
		if (contentType == null)
			contentType = ContentType.JMF_AMF;
		
		startTransports();
		startMessagingProtocol();
	}
	
	public void stop() {
		stopTransports();
		stopMessagingProtocol();
	}

	public RemotingChannel newRemotingChannel(String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(remotingTransport, id, uri);
		case JMF_AMF:
			return new JMFAMFRemotingChannel(remotingTransport, id, uri, sharedContext);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public RemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(remotingTransport, id, uri, maxConcurrentRequests);
		case JMF_AMF:
			return new JMFAMFRemotingChannel(remotingTransport, id, uri, sharedContext, maxConcurrentRequests);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public RemotingChannel newRemotingChannel(Configuration configuration, String id, URI uri, int maxConcurrentRequests) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(remotingTransport, configuration, id, uri, maxConcurrentRequests);
		case JMF_AMF:
			return new JMFAMFRemotingChannel(remotingTransport, configuration, id, uri, sharedContext, maxConcurrentRequests);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public MessagingChannel newMessagingChannel(String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFMessagingChannel(messagingTransport, id, uri);
		case JMF_AMF:
			return new JMFAMFMessagingChannel(messagingTransport, id, uri, sharedContext);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public MessagingChannel newMessagingChannel(Configuration configuration, String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFMessagingChannel(messagingTransport, configuration, id, uri);
		case JMF_AMF:
			return new JMFAMFMessagingChannel(messagingTransport, configuration, id, uri, sharedContext);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}
}
