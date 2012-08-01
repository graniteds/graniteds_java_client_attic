package org.granite.client.messaging.channel;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResponseListener;


public interface MessagingChannel extends Channel {
	
	void addConsumer(Consumer consumer);
	boolean removeConsumer(Consumer consumer);
	
	public ResponseMessageFuture disconnect(ResponseListener...listeners);
}
