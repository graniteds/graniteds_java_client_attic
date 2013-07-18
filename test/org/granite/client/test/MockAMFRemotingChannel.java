package org.granite.client.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.messaging.transport.TransportStopListener;

public class MockAMFRemotingChannel extends AMFRemotingChannel {

	public MockAMFRemotingChannel() {
		super(null, null, "test", URI.create("/temp"), RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
	}
	
	public TransportMessage createMessage(AsyncToken token) throws UnsupportedEncodingException {
		return createTransportMessage(token);
	}

	@SuppressWarnings("unused")
	private static class MockTransport implements Transport {

		@Override
		public void setContext(Object context) {
		}

		@Override
		public Object getContext() {
			return null;
		}

		@Override
		public void setConfiguration(Configuration config) {
		}

		@Override
		public Configuration getConfiguration() {
			return null;
		}

		@Override
		public boolean start() {
			return false;
		}

		@Override
		public boolean isStarted() {
			return false;
		}

		@Override
		public void stop() {
		}

		@Override
		public void setStatusHandler(TransportStatusHandler statusHandler) {
		}

		@Override
		public TransportStatusHandler getStatusHandler() {
			return null;
		}

		@Override
		public void addStopListener(TransportStopListener listener) {
		}

		@Override
		public boolean removeStopListener(TransportStopListener listener) {
			return false;
		}

		@Override
		public TransportFuture send(Channel channel, TransportMessage message)
				throws TransportException {
			return null;
		}

		@Override
		public void poll(Channel channel, TransportMessage message)
				throws TransportException {
		}
		
	}
}
