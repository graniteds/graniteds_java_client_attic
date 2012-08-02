package org.granite.client.messaging.events;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.messages.push.TopicMessage;

public class TopicMessageEvent implements IncomingMessageEvent<TopicMessage> {

	private final Consumer consumer;
	private final TopicMessage message;
	
	public TopicMessageEvent(Consumer consumer, TopicMessage message) {
		this.consumer = consumer;
		this.message = message;
	}

	@Override
	public Type getType() {
		return Type.TOPIC;
	}

	@Override
	public TopicMessage getMessage() {
		return message;
	}
	
	public Object getData() {
		return message.getData();
	}

	public Consumer getConsumer() {
		return consumer;
	}
	
	public String getTopic() {
		return consumer.getTopic();
	}
}
