package org.granite.client.messaging.channel.amf;

import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.codec.JMFAMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.transport.Transport;

import flex.messaging.messages.Message;

public class JMFAMFMessagingChannel extends AMFMessagingChannel {

	private final ClientSharedContext sharedContext;
	
	public JMFAMFMessagingChannel(Transport transport, String id, URI uri, ClientSharedContext sharedContext) {
		super(transport, id, uri);
		
		this.sharedContext = sharedContext;
	}

	public JMFAMFMessagingChannel(Transport transport, Configuration configuration, String id, URI uri, ClientSharedContext sharedContext) {
		super(transport, configuration, id, uri);
		
		this.sharedContext = sharedContext;
	}

	@Override
	protected MessagingCodec<Message[]> newMessagingCodec(Configuration configuration) {
		return new JMFAMF3MessagingCodec(configuration, sharedContext);
	}
}
