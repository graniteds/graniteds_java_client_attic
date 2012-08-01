package org.granite.client.messaging.channel;

import org.granite.client.messaging.Consumer;


public interface MessagingChannel extends Channel, SessionAwareChannel {

	void setSessionId(String sessionId);

	void addConsumer(Consumer consumer);
	boolean removeConsumer(Consumer consumer);
}
