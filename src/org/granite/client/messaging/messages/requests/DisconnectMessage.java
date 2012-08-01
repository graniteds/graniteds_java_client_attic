package org.granite.client.messaging.messages.requests;

import java.util.Map;

public final class DisconnectMessage extends AbstractRequestMessage {
	
	public DisconnectMessage() {
	}

	public DisconnectMessage(String clientId) {
		super(clientId);
	}

	public DisconnectMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}
	
	@Override
	public DisconnectMessage copy() {
		DisconnectMessage message = new DisconnectMessage();
		
		copy(message);
		
		return message;
	}

	@Override
	public Type getType() {
		return Type.DISCONNECT;
	}
}
