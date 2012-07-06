package org.granite.client.messaging.messages.requests;

import java.util.Map;

public final class PingMessage extends AbstractRequestMessage {
	
	public PingMessage() {
	}

	public PingMessage(String clientId) {
		super(clientId);
	}

	public PingMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}

	@Override
	public Type getType() {
		return Type.PING;
	}
	
	@Override
	public PingMessage copy() {
		PingMessage message = new PingMessage();
		
		copy(message);
		
		return message;
	}
}
