package org.granite.client.messaging.channel;

import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.platform.Platform;
import org.granite.util.ContentType;

public abstract class AbstractChannelFactory implements ChannelFactory {
	
	protected final ContentType contentType;
	
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;
	protected Object context = null;

	protected AbstractChannelFactory(ContentType contentType) {
		this(contentType, null, null);
	}

	protected AbstractChannelFactory(ContentType contentType, Transport remotingTransport, Transport messagingTransport) {
		this.contentType = contentType;
		this.remotingTransport = remotingTransport;
		this.messagingTransport = messagingTransport;
	}

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
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
			remotingTransport = Platform.getInstance().newRemotingTransport(context);
		
		if (!remotingTransport.isStarted() && !remotingTransport.start())
			throw new TransportException("Could not start remoting transport: " + remotingTransport);
		
		if (messagingTransport == null) {
			messagingTransport = Platform.getInstance().newMessagingTransport(context);
			if (messagingTransport == null)
				messagingTransport = remotingTransport;
		}
		else if (!messagingTransport.isStarted() && !messagingTransport.start())
			throw new TransportException("Could not start messaging transport: " + messagingTransport);
	}
	
	public void stop() {
		stop(true);
	}

	public void stop(boolean stopTransports) {
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
