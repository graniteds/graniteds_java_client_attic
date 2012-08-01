package org.granite.client.test.tide;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.test.MockRemoteService;
import org.granite.client.tide.server.ServerSession.ServiceFactory;

public final class MockServiceFactory implements ServiceFactory {
	@Override
	public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination) {
		return new MockRemoteService(remotingChannel, destination);
	}

	@Override
	public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic) {
		return null;
	}

	@Override
	public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic) {
		return null;
	}
}