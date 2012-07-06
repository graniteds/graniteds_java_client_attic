package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.Message;

public final class UnsubscribeMessage extends AbstractTopicRequestMessage {

	private String subscriptionId = null;
	
	public UnsubscribeMessage() {
	}

	public UnsubscribeMessage(String destination, String topic, String subscriptionId) {
		this(null, destination, topic, subscriptionId);
	}

	public UnsubscribeMessage(String clientId, String destination, String topic, String subscriptionId) {
		super(clientId, destination, topic);
		
		this.subscriptionId = subscriptionId;
	}

	public UnsubscribeMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic,
		String subscriptionId) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.subscriptionId = subscriptionId;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	@Override
	public Type getType() {
		return Type.UNSUBSCRIBE;
	}

	@Override
	public Message copy() {
		UnsubscribeMessage message = new UnsubscribeMessage();
		
		copy(message);
		
		message.subscriptionId = subscriptionId;
		
		return message;
	}
}
