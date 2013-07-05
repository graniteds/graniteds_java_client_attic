package org.granite.client.messaging.channel;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.DefaultConfiguration;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.util.ContentType;

public abstract class AbstractChannelFactory implements ChannelFactory {
	
	protected final ContentType contentType;
	
	protected Configuration configuration = null;
	
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;

	protected AbstractChannelFactory(ContentType contentType) {
		this(contentType, null, null, null);
	}

	protected AbstractChannelFactory(ContentType contentType, Configuration configuration, Transport remotingTransport, Transport messagingTransport) {
		this.contentType = contentType;
		this.remotingTransport = remotingTransport;
		this.messagingTransport = messagingTransport;
	}

	public ContentType getContentType() {
		return contentType;
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

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public void start() {
		
		if (configuration == null) {
			configuration = new DefaultConfiguration();
			configuration.load();
		}
		
		if (remotingTransport == null)
			remotingTransport = new ApacheAsyncTransport();
		
		if (!remotingTransport.isStarted() && !remotingTransport.start())
			throw new TransportException("Could not start remoting transport: " + remotingTransport);
		
		if (messagingTransport == null)
			messagingTransport = remotingTransport;
		else if (!messagingTransport.isStarted() && !messagingTransport.start())
			throw new TransportException("Could not start messaging transport: " + messagingTransport);
	}
	
	public void stop() {
		stop(true);
	}

	public void stop(boolean stopTransports) {
		configuration = null;
		
		if (stopTransports) {
			if (remotingTransport != null && remotingTransport.isStarted()) {
				remotingTransport.stop();
				remotingTransport = null;
			}
			
			if (messagingTransport != null && messagingTransport.isStarted()) {
				messagingTransport.stop();
				messagingTransport = null;
			}
		}
	}
}
