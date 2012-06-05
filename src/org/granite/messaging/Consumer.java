package org.granite.messaging;

import java.nio.charset.Charset;

import org.granite.rpc.AsyncResponder;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

public class Consumer implements MessageAgent, MessageListener {
	
	private WebSocketChannel channel;
	private String destination;
	private String topic;
	private String selector;
	private MessageListener messageListener;
	private SubscriptionListener subscriptionListener;
	
    
    public Consumer(String destination) {
        this.destination = destination;
    }
	
	public Consumer(WebSocketChannel channel, String destination) {
		this.destination = destination;
		setChannel(channel);
	}
	
	public void setChannel(WebSocketChannel channel) {
		if (this.channel != null)
			this.channel.removeMessageListener(this);
	    this.channel = channel;
	    if (channel != null)
	    	channel.addMessageListener(this);
	}
	
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}
	
	public void setSubscriptionListener(SubscriptionListener subscriptionListener) {
		this.subscriptionListener = subscriptionListener;
	}

	public WebSocketChannel getChannel() {
		return channel;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public void setCredentials(String username, String password) {
		setCredentials(username, password, null);
	}

	public void setCredentials(String username, String password, Charset charset) {
		if (username == null || password == null)
			throw new NullPointerException("Username and password must not be null");
		channel.setCredentials(username + ':' + password, charset);
	}

	public boolean isAuthenticated() {
		return channel.isAuthenticated();
	}
	
	private CommandMessage subscribeMessage = null;
	private CommandMessage unsubscribeMessage = null;
	private boolean subscribed = false;
	
	public void subscribe() {
		subscribe(null);
	}
	
	public void subscribe(String subscriptionId) {
		CommandMessage cmdMessage = new CommandMessage();
		cmdMessage.setOperation(CommandMessage.SUBSCRIBE_OPERATION);
		cmdMessage.setDestination(destination);
		cmdMessage.setMessageId(UUIDUtil.randomUUID());
		cmdMessage.setHeader(CommandMessage.SUBTOPIC_HEADER, topic);
		cmdMessage.setHeader(CommandMessage.SELECTOR_HEADER, selector);
		cmdMessage.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
		subscribeMessage = cmdMessage;
		channel.send(cmdMessage);
	}
	
	public void unsubscribe() {
		CommandMessage cmdMessage = new CommandMessage();
		cmdMessage.setOperation(CommandMessage.UNSUBSCRIBE_OPERATION);
		cmdMessage.setDestination(destination);
		cmdMessage.setMessageId(UUIDUtil.randomUUID());
		unsubscribeMessage = cmdMessage;
		channel.send(cmdMessage);
	}
	
	public boolean isSubscribed() {
		return subscribed;
	}
	
	public void logout(AsyncResponder responder) {
		channel.logout(responder);
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof AcknowledgeMessage) {
			AcknowledgeMessage ackMessage = (AcknowledgeMessage)message;
			if (subscribeMessage != null && ackMessage.getCorrelationId().equals(subscribeMessage.getMessageId())) {
				subscribed = true;
				if (subscriptionListener != null)
					subscriptionListener.onSubscribeSuccess(ackMessage, subscribeMessage);
			}
			else if (unsubscribeMessage != null && ackMessage.getCorrelationId().equals(unsubscribeMessage.getMessageId())) {
				subscribed = false;
				if (subscriptionListener != null)
					subscriptionListener.onUnsubscribeSuccess(ackMessage, unsubscribeMessage);
			}
		}
		else if (message instanceof ErrorMessage) {
			ErrorMessage errorMessage = (ErrorMessage)message;
			if (unsubscribeMessage != null && errorMessage.getCorrelationId().equals(subscribeMessage.getMessageId())) {
				subscribed = false;
				if (subscriptionListener != null)
					subscriptionListener.onSubscribeFault(errorMessage.getFaultCode(), errorMessage.getFaultString(), errorMessage.getFaultDetail(), errorMessage);
			}
			else if (unsubscribeMessage != null && errorMessage.getCorrelationId().equals(unsubscribeMessage.getMessageId())) {
				subscribed = false;
				if (subscriptionListener != null)
					subscriptionListener.onSubscribeFault(errorMessage.getFaultCode(), errorMessage.getFaultString(), errorMessage.getFaultDetail(), errorMessage);
			}
		}
		else if (messageListener != null)
			messageListener.onMessage(message);
	}

	public interface SubscriptionListener {
		
		public void onSubscribeSuccess(AcknowledgeMessage ackMessage, CommandMessage subscriptionMessage);
		
		public void onSubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message);
		
		public void onUnsubscribeSuccess(AcknowledgeMessage message, CommandMessage unsubscriptionMessage);
		
		public void onUnsubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message);
	}
}
