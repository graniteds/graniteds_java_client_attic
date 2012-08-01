package org.granite.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;


public class MockRemoteService extends RemoteService {
    
    private static ResponseBuilder responseBuilder = null;
    
    private static Executor executor = Executors.newSingleThreadExecutor();
    
    public MockRemoteService(RemotingChannel remotingChannel, String destination) {
    	super(remotingChannel, destination);
    }
    
    public static void setResponseBuilder(ResponseBuilder rb) {
    	responseBuilder = rb;
    }
    
    @Override
	public RemoteServiceInvocation newInvocation(String method, Object...parameters) {
		return new MockRemoteServiceInvocation(method, parameters);
	}
    
	public class MockRemoteServiceInvocation extends RemoteServiceInvocation {
		
		private final InvocationMessage request;
		private final List<ResponseListener> listeners = new ArrayList<ResponseListener>();
		
		public MockRemoteServiceInvocation(final String method, final Object...parameters) {
			super(MockRemoteService.this, method, parameters);
			request = new InvocationMessage(getId(), method, parameters);
		}

		@Override
		public RemoteServiceInvocationChain appendInvocation(String method, Object... parameters) {
			return null;
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
		
		@Override
		public ResponseMessageFuture invoke() {
			final AsyncToken token = new AsyncToken(request, listeners.toArray(new ResponseListener[listeners.size()]));
			executor.execute(new Runnable() {
				@Override
				public void run() {
					ResultMessage result = (ResultMessage)responseBuilder.buildResponseMessage(MockRemoteService.this, request);
					token.dispatchResult(result);
				}
			});
			return token;
		}
	}

}
