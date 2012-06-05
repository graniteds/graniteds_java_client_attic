/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.granite.logging.Logger;
import org.granite.messaging.engine.EngineException;
import org.granite.messaging.engine.EngineMessageHandler;
import org.granite.messaging.engine.EngineStatusHandler;
import org.granite.messaging.engine.WebSocketEngine;
import org.granite.messaging.engine.WebSocketEngine.Connection;
import org.granite.rpc.AsyncResponder;
import org.granite.util.Base64;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class WebSocketChannel implements EngineMessageHandler  {
	
	private static final Logger log = Logger.getLogger(WebSocketChannel.class);

    public static final String RECONNECT_INTERVAL_MS_KEY = "reconnect-interval-ms";
    public static final String RECONNECT_MAX_ATTEMPTS_KEY = "reconnect-max-attempts";
    
	private final WebSocketEngine engine;
	private final String id;
	private final URI uri;

	private String clientId = null;
	private String sessionId = null;

	private String credentials = null;
	private boolean authenticated = false;
	private boolean authenticating = false;

	private Lock connectionLock = new ReentrantLock();
	private boolean connecting = false;
	private boolean connected = false;

	private int maxIdleTime = 300000;
	private int reconnectMaxAttempts = 5;
	private int reconnectIntervalMillis = 60000;
	
	
	public WebSocketChannel(WebSocketEngine engine, String id, URI uri) {
		if (engine == null)
			throw new NullPointerException("Engine cannot be null");
		if (uri == null)
			throw new NullPointerException("URI cannot be null");
		
		this.engine = engine;
		this.engine.setStatusHandler(new ChannelStatusHandler(engine.getStatusHandler()));
		this.id = id;
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
	}
	
	public void setSessionId(String sessionId) {
		if ((sessionId == null && this.sessionId != null) || !sessionId.equals(this.sessionId)) {
			this.sessionId = sessionId;
			log.info("Received sessionId %s", sessionId);
		}				
	}
	
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
	public void setCredentials(String credentials, Charset charset) {
		if (charset == null)
			charset = Charset.defaultCharset();
		try {
			this.credentials = Base64.encodeToString(credentials.getBytes(charset.name()), false);
		}
		catch (UnsupportedEncodingException e) {
			log.error(e, "Unsupported credentials encoding");
		}
	}

	public boolean isAuthenticated() {
		return authenticated;
	}
	
	public void logout(AsyncResponder responder) {
		this.credentials = null;
		this.authenticated = false;
		this.authenticating = false;
		
		CommandMessage message = new CommandMessage();
		message.setOperation(CommandMessage.LOGOUT_OPERATION);
		message.setMessageId(UUIDUtil.randomUUID());
	}
	
	
	private Connection connection;
	
	private List<MessageListener> messageListeners = new ArrayList<MessageListener>();
	
	public void addMessageListener(MessageListener messageListener) {
		messageListeners.add(messageListener);
	}

	public void removeMessageListener(MessageListener messageListener) {
		messageListeners.remove(messageListener);
	}

	
	private void connect() {		
		if (connecting)
			return;
		
		connecting = true;
		connected = true;
		
		engine.setMaxIdleTime(maxIdleTime);
		engine.connect(uri, this, clientId, sessionId);
	}
	
	
	@Override
	public void onConnect(Connection connection) {
		this.connection = connection;
		this.connecting = false;
		this.reconnectAttempts = 0;
		
		// Wait for the initial acknowledge message
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof AcknowledgeMessage && "OPEN_CONNECTION".equals(((AcknowledgeMessage)message).getCorrelationId())) {
			clientId = (String)message.getClientId();
	        @SuppressWarnings("unchecked")
			Map<String, Object> advice = (Map<String, Object>)message.getBody();
	        if (advice != null) {
	        	try {
	        		// The advice may be received as Double or Long depending on Long externalization strategy
	        		this.reconnectIntervalMillis = ((Number)advice.get(RECONNECT_INTERVAL_MS_KEY)).intValue();
	        		this.reconnectMaxAttempts = ((Number)advice.get(RECONNECT_MAX_ATTEMPTS_KEY)).intValue();
	        	}
	        	catch (NumberFormatException e) {
	        		log.error("Wrong advice received " + advice, e);
	        	}
	        }
			log.info("Connection opened, received clientId %s", clientId);			
			
			internalSend();
			return;
		}
		
		for (MessageListener listener : messageListeners)
			listener.onMessage(message);
	}
	
	private int reconnectAttempts = 0;

	@Override
	public void onDisconnect(int closeCode, String message) {
		// Mark the connection as close, the channel should reopen a connection for the next message
		this.connection = null;
		this.connecting = false;
		
		if (!engine.isStarted())
			this.connected = false;
		
		if (clientId == null) {
			engine.getStatusHandler().handleException(new EngineException("Channel could not connect code: " + closeCode + " " + message));
			return;
		}
		
		if (connected) {
			if (reconnectAttempts >= reconnectMaxAttempts) {
				this.connected = false;
				if (engine.isStarted())
					engine.stop();
				
				engine.getStatusHandler().handleException(new EngineException("Channel disconnected"));
				return;
			}
			
			try {
				Thread.sleep(reconnectIntervalMillis);
			}
			catch (InterruptedException e) {
			}
			
			reconnectAttempts++;
			
			// If the channel should be connected, try to reconnect
			log.info("Connection lost (code %d, msg %s), reconnect channel (retry #%d)", closeCode, message, reconnectAttempts);
			connect();
		}
	}
	
	
	private Lock messagesLock = new ReentrantLock();
	private List<Message> pendingMessages = new ArrayList<Message>();
	
	public void send(Message message) {
		messagesLock.lock();
		try {
			pendingMessages.add(message);
		}
		finally {
			messagesLock.unlock();
		}
		
		internalSend();
	}
	
	private void internalSend() {
		connectionLock.lock();
		try {
			if (!engine.isStarted())
				engine.start();
			
			if (connection == null)
				connect();
			else {
				List<Message> messages = null;
				messagesLock.lock();
				try {
					if (pendingMessages.size() == 0)
						return;
					
					messages = pendingMessages;
					pendingMessages = new ArrayList<Message>();
				}
				finally {
					messagesLock.unlock();
				}
				
				if (!authenticated && !authenticating && credentials != null) {
					authenticating = true;
					
					CommandMessage loginMessage = new CommandMessage();
					loginMessage.setOperation(CommandMessage.LOGIN_OPERATION);
					loginMessage.setMessageId(UUIDUtil.randomUUID());
					loginMessage.setBody(credentials);
					messages.add(0, loginMessage);
				}
				
				try {
					for (int i = 0; i < messages.size(); i++) {
						Message message = messages.get(i);
						
						if (id != null) {
							if (message.getHeaders() == null)
								message.setHeaders(new HashMap<String, Object>());
							message.setHeader(Message.ENDPOINT_HEADER, id);
						}
						message.setClientId(clientId);
					}
					
					connection.send(messages.toArray(new Message[messages.size()]));
				}
				catch (Exception e) {
					engine.getStatusHandler().handleException(new EngineException("Client error", e));
					throw e;
				}
			}
		}
		catch (Exception e) {
			engine.getStatusHandler().handleException(new EngineException("Channel failed", e));
		}
		finally {
			connectionLock.unlock();
		}
	}
	
	
	public class ChannelStatusHandler implements EngineStatusHandler {
		
		private final EngineStatusHandler wrappedHandler;
		
		public ChannelStatusHandler(EngineStatusHandler wrappedHandler) {
			this.wrappedHandler = wrappedHandler;
		}

		@Override
		public void handleIO(boolean active) {
			if (wrappedHandler != null)
				wrappedHandler.handleIO(active);
		}

		@Override
		public void handleException(EngineException e) {
			if (wrappedHandler != null)
				wrappedHandler.handleException(e);
		}
		
	}
}
