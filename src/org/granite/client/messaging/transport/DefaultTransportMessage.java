package org.granite.client.messaging.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.client.messaging.codec.MessagingCodec;

public class DefaultTransportMessage<M> implements TransportMessage {

	private final String id;
	private final boolean connect;
	private final String clientId;
	private final String sessionId;
	private final M message;
	private final MessagingCodec<M> codec;

	public DefaultTransportMessage(String id, boolean connect, String clientId, String sessionId, M message, MessagingCodec<M> codec) {
		this.id = id;
		this.connect = connect;
		this.clientId = clientId;
		this.sessionId = sessionId;
		this.message = message;
		this.codec = codec;
	}

	public String getId() {
		return id;
	}
	
	public boolean isConnect() {
		return connect;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public String getContentType() {
		return codec.getContentType();
	}

	@Override
	public void encode(OutputStream os) throws IOException {
		codec.encode(message, os);
	}
}
