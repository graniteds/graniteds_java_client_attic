package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.Message;

public final class SubscribeMessage extends AbstractTopicRequestMessage {

	private String selector = null;
	
	public SubscribeMessage() {
	}

	public SubscribeMessage(String destination, String topic, String selector) {
		this(null, destination, topic, selector);
	}

	public SubscribeMessage(String clientId, String destination, String topic, String selector) {
		super(clientId, destination, topic);
		
		this.selector = selector;
	}

	public SubscribeMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination, String topic,
		String selector) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.selector = selector;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@Override
	public Type getType() {
		return Type.SUBSCRIBE;
	}

	@Override
	public Message copy() {
		SubscribeMessage message = new SubscribeMessage();
		
		copy(message);
		
		message.selector = selector;
		
		return message;
	}
}
