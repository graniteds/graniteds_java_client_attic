package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.RequestMessage;

public abstract class AbstractRequestMessage extends AbstractMessage implements RequestMessage {

	public AbstractRequestMessage() {
	}

	public AbstractRequestMessage(String clientId) {
		super(clientId);
	}

	public AbstractRequestMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {
		
		super(id, clientId, timestamp, timeToLive, headers);
	}
}
