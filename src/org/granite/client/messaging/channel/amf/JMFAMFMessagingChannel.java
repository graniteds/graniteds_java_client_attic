package org.granite.client.messaging.channel.amf;

import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.codec.JMFAMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;

import flex.messaging.messages.Message;

public class JMFAMFMessagingChannel extends AMFMessagingChannel {

	public JMFAMFMessagingChannel(Transport transport, String id, URI uri) {
		super(transport, id, uri);
	}

	public JMFAMFMessagingChannel(Transport transport, Configuration configuration, String id, URI uri) {
		super(transport, configuration, id, uri);
	}

	@Override
	protected MessagingCodec<Message[]> newMessagingCodec(Configuration configuration) {
		return new JMFAMF3MessagingCodec(configuration);
	}
}
