package org.granite.client.messaging.messages.push;

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.Message;

public class TopicMessage extends AbstractMessage {

	private Object data;
	
	public TopicMessage() {
	}

	public TopicMessage(String clientId, Object data) {
		super(clientId);
		
		this.data = data;
	}

	public TopicMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		Object data) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.data = data;
	}
	
	@Override
	public Type getType() {
		return Type.TOPIC;
	}

	@Override
	public Message copy() {
		TopicMessage message = new TopicMessage();

		super.copy(message);
		
		return message;
	}
	
	public Object getData() {
		return data;
	}
}
