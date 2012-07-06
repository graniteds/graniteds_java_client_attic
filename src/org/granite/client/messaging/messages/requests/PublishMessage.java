package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.Message;

public final class PublishMessage extends AbstractTopicRequestMessage {

	private Object body = null;

	public PublishMessage() {
	}

	public PublishMessage(String destination, String topic, Object body) {
		this(null, destination, topic, body);
	}

	public PublishMessage(String clientId, String destination, String topic, Object body) {
		super(clientId, destination, topic);
		
		this.body = body;
	}

	public PublishMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic,
		Object body) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.body = body;
	}

	@Override
	public Type getType() {
		return Type.PUBLISH;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public Message copy() {
		PublishMessage message = new PublishMessage();
		
		copy(message);
		
		return message;
	}
}
