package org.granite.client.messaging;

import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;

public interface TopicAgent {

	MessagingChannel getChannel();

	String getDestination();

	String getTopic();
	
	Map<String, Object> getDefaultHeaders();

}
