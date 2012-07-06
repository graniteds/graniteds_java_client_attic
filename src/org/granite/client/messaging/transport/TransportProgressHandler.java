package org.granite.client.messaging.transport;

public interface TransportProgressHandler<M> {

	void sending(M message, int sent, int total);
	void receiving(M message, int received, int total);
}
