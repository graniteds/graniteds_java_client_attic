package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;

public abstract class AbstractTopicRequestMessage extends AbstractRequestMessage {

	private String destination = null;
	private String topic = null;

	public AbstractTopicRequestMessage() {
	}

	public AbstractTopicRequestMessage(String destination, String topic) {
		this(null, destination, topic);
	}

	public AbstractTopicRequestMessage(String clientId, String destination, String topic) {
		super(clientId);
		
		this.destination = destination;
		this.topic = topic;
	}

	public AbstractTopicRequestMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.destination = destination;
		this.topic = topic;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	protected void copy(AbstractMessage message) {
		super.copy(message);
		
		((AbstractTopicRequestMessage)message).destination = destination;
		((AbstractTopicRequestMessage)message).topic = topic;
	}
}
