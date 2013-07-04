package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.transport.Transport;
import org.granite.util.ContentType;

public class JMFChannelFactory extends AbstractChannelFactory {

	private ClientSharedContext sharedContext = null;
	
	public JMFChannelFactory() {
		super(ContentType.JMF_AMF);
	}

	public JMFChannelFactory(Transport remotingTransport, Transport messagingTransport) {
		super(ContentType.JMF_AMF, remotingTransport, messagingTransport);

	}

	@Override
	public void start() {
		super.start();
		
		if (sharedContext == null) {
			//sharedContext = new DefaultCli
		}
	}

	@Override
	public RemotingChannel newRemotingChannel(String id, URI uri) {
		return null;
	}

	@Override
	public RemotingChannel newRemotingChannel(String id, URI uri,
			int maxConcurrentRequests) {
		return null;
	}

	@Override
	public MessagingChannel newMessagingChannel(String id, URI uri) {
		return null;
	}

}
