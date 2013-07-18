package org.granite.client.messaging.channel;

import java.util.Set;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.messaging.AliasRegistry;
import org.granite.util.ContentType;


public abstract class AbstractChannelFactory implements ChannelFactory {
	
	protected final ContentType contentType;
	
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;

	private boolean scanRemoteAliases = true;
	private Set<String> scanPackageNames = null;
	protected AliasRegistry aliasRegistry = null;

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

	public boolean getScanRemoteAliases() {
		return scanRemoteAliases;
	}

	public void setScanRemoteAliases(boolean scanRemoteAliases) {
		this.scanRemoteAliases = scanRemoteAliases;
	}

	public void setScanPackageNames(Set<String> packageNames) {
		this.scanPackageNames = packageNames;
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
		
		if (aliasRegistry == null)
			aliasRegistry = new ClientAliasRegistry();
		
		if (scanRemoteAliases)
			aliasRegistry.scan(scanPackageNames);
	}
	
	public void stop() {
		aliasRegistry = null;
		
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
