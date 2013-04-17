package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.channel.amf.JMFAMFMessagingChannel;
import org.granite.client.messaging.channel.amf.JMFAMFRemotingChannel;
import org.granite.client.messaging.transport.Transport;
import org.granite.util.ContentType;

public class ChannelFactory {

	private final ContentType contentType;
	
	public ChannelFactory(ContentType contentType) {
		this.contentType = contentType;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public RemotingChannel newRemotingChannel(Transport transport, String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(transport, id, uri);
		case JMF_AMF:
			return new JMFAMFRemotingChannel(transport, id, uri);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public RemotingChannel newRemotingChannel(Transport transport, String id, URI uri, int maxConcurrentRequests) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(transport, id, uri, maxConcurrentRequests);
			
		case JMF_AMF:
			return new JMFAMFRemotingChannel(transport, id, uri, maxConcurrentRequests);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public RemotingChannel newRemotingChannel(Transport transport, Configuration configuration, String id, URI uri, int maxConcurrentRequests) {
		switch (contentType) {
		case AMF:
			return new AMFRemotingChannel(transport, configuration, id, uri, maxConcurrentRequests);
			
		case JMF_AMF:
			return new JMFAMFRemotingChannel(transport, configuration, id, uri, maxConcurrentRequests);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public MessagingChannel newMessagingChannel(Transport transport, String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFMessagingChannel(transport, id, uri);
		case JMF_AMF:
			return new JMFAMFMessagingChannel(transport, id, uri);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}

	public MessagingChannel newMessagingChannel(Transport transport, Configuration configuration, String id, URI uri) {
		switch (contentType) {
		case AMF:
			return new AMFMessagingChannel(transport, configuration, id, uri);
		case JMF_AMF:
			return new JMFAMFMessagingChannel(transport, configuration, id, uri);
		default:
			throw new UnsupportedOperationException("Unsupported content-type: " + contentType);
		}
	}
}
