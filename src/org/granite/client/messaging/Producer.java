package org.granite.client.messaging;

import java.util.Map;

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

	public ResponseMessageFuture publish(Object message, Map<String, Object> headers, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		publishMessage.getHeaders().putAll(headers);
		return channel.send(publishMessage, listeners);
	}
}
