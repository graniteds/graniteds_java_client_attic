package org.granite.client.messaging;

import flex.messaging.messages.Message;

public interface MessageListener {
	
	public void onMessage(Message message);
}