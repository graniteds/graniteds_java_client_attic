package org.granite.client.messaging;

import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.requests.SubscribeMessage;
import org.granite.client.messaging.messages.requests.UnsubscribeMessage;
import org.granite.logging.Logger;

public class Consumer extends AbstractTopicAgent implements MessageListener {
	
	private static final Logger log = Logger.getLogger(Consumer.class);

	private final ConcurrentHashMap<MessageListener, Boolean> listeners = new ConcurrentHashMap<MessageListener, Boolean>();
	
	private String subscriptionId = null;
	private String selector = null;
	
	public Consumer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
	
	public boolean isSubscribed() {
		return subscriptionId != null;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public ResponseMessageFuture subscribe(ResponseListener...listeners) {
		SubscribeMessage subscribeMessage = new SubscribeMessage(destination, topic, selector);
		subscribeMessage.getHeaders().putAll(defaultHeaders);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				subscriptionId = (String)event.getResult();
				channel.addConsumer(consumer);
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Subscription failed %s: %s", consumer, event);
			}
		};
		
		if (listeners == null || listeners.length == 0)
			listeners = new ResponseListener[]{listener};
		else {
			ResponseListener[] tmp = new ResponseListener[listeners.length + 1];
			System.arraycopy(listeners, 0, tmp, 0, listeners.length);
			tmp[listeners.length] = listener;
			listeners = tmp;
		}
		
		return channel.send(subscribeMessage, listeners);
	}

	public ResponseMessageFuture unsubscribe(ResponseListener...listeners) {
		UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage(destination, topic, subscriptionId);
		unsubscribeMessage.getHeaders().putAll(defaultHeaders);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				channel.removeConsumer(consumer);
				subscriptionId = null;
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Unsubscription failed %s: %s", consumer, event);
			}
		};
		
		if (listeners == null || listeners.length == 0)
			listeners = new ResponseListener[]{listener};
		else {
			ResponseListener[] tmp = new ResponseListener[listeners.length + 1];
			System.arraycopy(listeners, 0, tmp, 0, listeners.length);
			tmp[listeners.length] = listener;
			listeners = tmp;
		}

		return channel.send(unsubscribeMessage, listeners);
	}

	public void addMessageListener(MessageListener listener) {
		listeners.putIfAbsent(listener, Boolean.TRUE);
	}
	
	public boolean removeMessageListener(MessageListener listener) {
		return listeners.remove(listener) != null;
	}
	
	public void onDisconnect() {
		subscriptionId = null;
	}

	@Override
	public void onMessage(Message message) {
		for (MessageListener listener : listeners.keySet()) {
			try {
				listener.onMessage(message);
			}
			catch (Exception e) {
				log.error(e, "Consumer listener threw an exception: ", listener);
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + " {subscriptionId=" + subscriptionId +
			", destination=" + destination +
			", topic=" + topic +
			", selector=" + selector +
		"}";
	}
}
