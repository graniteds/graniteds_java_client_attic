package org.granite.client.messaging;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.PublishMessage;

public class Producer extends AbstractTopicAgent {

	public Producer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

	public ResponseMessageFuture publish(Object message, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		return channel.send(publishMessage, listeners);
	}
}
