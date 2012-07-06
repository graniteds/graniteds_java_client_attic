package org.granite.client.messaging.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.client.messaging.codec.MessagingCodec;

public class DefaultTransportMessage<M> implements TransportMessage {

	private final String id;
	private final M message;
	private final MessagingCodec<M> codec;

	public DefaultTransportMessage(String id, M message, MessagingCodec<M> codec) {
		this.id = id;
		this.message = message;
		this.codec = codec;
	}

	public String getId() {
		return id;
	}

	public String getContentType() {
		return codec.getContentType();
	}

	@Override
	public void encode(OutputStream os) throws IOException {
		codec.encode(message, os);
	}
}
