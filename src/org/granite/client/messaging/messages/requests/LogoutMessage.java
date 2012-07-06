package org.granite.client.messaging.messages.requests;

import java.util.Map;

public final class LogoutMessage extends AbstractRequestMessage {
	
	public LogoutMessage() {
	}

	public LogoutMessage(String clientId) {
		super(clientId);
	}

	public LogoutMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}
	
	@Override
	public LogoutMessage copy() {
		LogoutMessage message = new LogoutMessage();
		
		copy(message);
		
		return message;
	}

	@Override
	public Type getType() {
		return Type.LOGOUT;
	}
}
