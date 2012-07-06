package org.granite.client.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.InvocationMessage;

public class RemoteService {

	private final Channel channel;
	private final String id;

	public RemoteService(Channel channel, String id) {
		if (channel == null || id == null)
			throw new NullPointerException("channel and id cannot be null");
		this.channel = channel;
		this.id = id;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getId() {
		return id;
	}
	
	public RemoteServiceInvocation newInvocation(String method, Object...parameters) {
		return new RemoteServiceInvocation(this, method, parameters);
	}
	
	public static interface RemoteServiceInvocationChain {
		
		RemoteServiceInvocationChain appendInvocation(String method, Object...parameters);
		ResponseMessageFuture invoke();
	}
	
	public static class RemoteServiceInvocation implements RemoteServiceInvocationChain {
		
		private final RemoteService remoteService;
		private final InvocationMessage request;
		
		private long timeToLive = 0L;
		private List<ResponseListener> listeners = new ArrayList<ResponseListener>();
		
		public RemoteServiceInvocation(RemoteService remoteService, String method, Object...parameters) {
			if (remoteService == null)
				throw new NullPointerException("remoteService cannot be null");
			this.remoteService = remoteService;
			this.request = new InvocationMessage(null, remoteService.id, method, parameters);
		}
		
		public RemoteServiceInvocation addListener(ResponseListener listener) {
			if (listener != null)
				this.listeners.add(listener);
			return this;
		}
		
		public RemoteServiceInvocation addListeners(ResponseListener...listeners) {
			if (listeners != null && listeners.length > 0)
				this.listeners.addAll(Arrays.asList(listeners));
			return this;
		}
		
		public RemoteServiceInvocation setTimeToLive(long timeToLiveMillis) {
			return setTimeToLive(timeToLiveMillis, TimeUnit.MILLISECONDS);
		}
		
		public RemoteServiceInvocation setTimeToLive(long timeToLive, TimeUnit unit) {
			if (timeToLive < 0)
				throw new IllegalArgumentException("timeToLive cannot be negative");
			if (unit == null)
				throw new NullPointerException("unit cannot be null");
			this.timeToLive = unit.toMillis(timeToLive);
			return this;
		}
		
		@Override
		public RemoteServiceInvocationChain appendInvocation(String method, Object...parameters) {
			InvocationMessage message = request;
			while (message.getNext() != null)
				message = message.getNext();
			message.setNext(new InvocationMessage(null, remoteService.id, method, parameters));
			return this;
		}
		
		@Override
		public ResponseMessageFuture invoke() {
			request.setTimeToLive(timeToLive);
			return remoteService.channel.send(request, listeners.toArray(new ResponseListener[listeners.size()]));
		}
	}
}
