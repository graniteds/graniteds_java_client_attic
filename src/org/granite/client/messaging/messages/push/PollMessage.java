package org.granite.client.messaging.messages.push;

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.Message;

public final class PollMessage extends AbstractMessage {

	public PollMessage() {
	}

	public PollMessage(String clientId) {
		super(clientId);
	}

	public PollMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}

	@Override
	public Type getType() {
		return Type.POLL;
	}

	@Override
	public Message copy() {
		return null;
	}
}
