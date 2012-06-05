package org.granite.messaging;

import java.nio.charset.Charset;

import org.granite.rpc.AsyncResponder;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

public class Producer implements MessageAgent {
	
	private WebSocketChannel channel;
	private String destination;
	private String topic;
    
    public Producer(String destination) {
        this.destination = destination;
    }
	
	public Producer(WebSocketChannel channel, String destination) {
		this.channel = channel;
		this.destination = destination;
	}
	
	public void setChannel(WebSocketChannel channel) {
	    this.channel = channel;
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
	
	public void logout(AsyncResponder responder) {
		channel.logout(responder);
	}
	
	public void send(Message message) {
		message.setDestination(destination);
		if (topic != null)
			message.setHeader(AsyncMessage.SUBTOPIC_HEADER, topic);
		message.setMessageId(UUIDUtil.randomUUID());
		message.setTimestamp(System.currentTimeMillis());
		channel.send(message);
	}

}
