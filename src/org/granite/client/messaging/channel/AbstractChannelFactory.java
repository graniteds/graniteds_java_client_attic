package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.util.ContentType;

public abstract class AbstractChannelFactory {
	
	protected final ContentType contentType;
	
	protected volatile Transport remotingTransport = null;
	protected volatile Transport messagingTransport = null;

	protected AbstractChannelFactory(ContentType contentType) {
		this(contentType, null, null);
	}

	protected AbstractChannelFactory(ContentType contentType, Transport remotingTransport, Transport messagingTransport) {
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
	
	public void start() {
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
		if (remotingTransport != null && remotingTransport.isStarted()) {
			remotingTransport.stop();
			remotingTransport = null;
		}
		
		if (messagingTransport != null && messagingTransport.isStarted()) {
			messagingTransport.stop();
			messagingTransport = null;
		}
	}
	
	public abstract RemotingChannel newRemotingChannel(String id, URI uri);
	public abstract RemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests);
	
	public abstract MessagingChannel newMessagingChannel(String id, URI uri);
}
