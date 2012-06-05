package org.granite.messaging;

import flex.messaging.messages.Message;

public interface MessageListener {
	
	public void onMessage(Message message);
}