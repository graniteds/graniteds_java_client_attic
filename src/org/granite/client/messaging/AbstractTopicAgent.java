package org.granite.client.messaging;

import java.util.HashMap;
import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;

public abstract class AbstractTopicAgent implements TopicAgent {
	
	protected final MessagingChannel channel;
	protected final String destination;
	protected final String topic;
	protected final Map<String, Object> defaultHeaders = new HashMap<String, Object>();

	public AbstractTopicAgent(MessagingChannel channel, String destination, String topic) {
		if (channel == null || destination == null)
			throw new NullPointerException("channel and destination cannot be null");
		this.channel = channel;
		this.destination = destination;
		this.topic = topic;
	}

	@Override
	public MessagingChannel getChannel() {
		return channel;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public String getTopic() {
		return topic;
	}
	
	@Override
	public Map<String, Object> getDefaultHeaders() {
		return defaultHeaders;
	}
}
