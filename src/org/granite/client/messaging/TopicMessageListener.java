package org.granite.client.messaging;

import org.granite.client.messaging.events.TopicMessageEvent;

public interface TopicMessageListener {
	
	void onMessage(TopicMessageEvent event);
}
