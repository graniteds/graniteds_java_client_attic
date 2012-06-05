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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.granite.logging.Logger;
import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;
import org.granite.messaging.engine.EngineException;
import org.granite.messaging.engine.EngineResponseHandler;
import org.granite.messaging.engine.HttpClientEngine;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.AsyncToken;
<<<<<<< HEAD
=======
<<<<<<< HEAD
import org.granite.rpc.events.AbstractEvent;
=======
>>>>>>> 6e5c8c0... Tide implementation for Java client
>>>>>>> master
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.MessageEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.util.Base64;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class Channel {
	
	private static final Logger log = Logger.getLogger(Channel.class);

	private final HttpClientEngine engine;
	private final String id;
	private final URI uri;
	
	private String credentials = null;
	private boolean authenticated = false;
	private boolean authenticating = false;

	private int index = 1;
	
	private Lock connectionLock = new ReentrantLock();
	private boolean connecting = false;
	private URI connectedUri = null;
	
	private Lock respondersLock = new ReentrantLock();
	private List<AsyncToken> pendingTokens = new ArrayList<AsyncToken>();
	
	private final ConcurrentHashMap<String, AsyncToken> activeTokens = new ConcurrentHashMap<String, AsyncToken>();
	
	public Channel(HttpClientEngine engine, String id, URI uri) {
		if (engine == null)
			throw new NullPointerException("Engine cannot be null");
		if (uri == null)
			throw new NullPointerException("Uri cannot be null");
		
		this.engine = engine;
		this.id = id;
		this.uri = uri;
	}

	public URI getUri() {
		return uri;
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
		
		AsyncToken token = new AsyncToken(message);
		if (responder != null)
			token.addResponder(responder);
		send(token);
	}
	
	public void send(AsyncToken token) {
		respondersLock.lock();
		try {
			pendingTokens.add(token);
		}
		finally {
			respondersLock.unlock();
		}
		
		internalSend();
	}
	
	protected void internalSend() {

		connectionLock.lock();
		try {
			if (!engine.isStarted())
				engine.start();
			
			if (connectedUri == null)
				connect(uri);
			else {
				List<AsyncToken> tokens = null;
				respondersLock.lock();
				try {
					if (pendingTokens.size() == 0)
						return;
					
					tokens = pendingTokens;
					pendingTokens = new ArrayList<AsyncToken>();
				}
				finally {
					respondersLock.unlock();
				}
				
				if (!authenticated && !authenticating && credentials != null) {
					authenticating = true;
					
					CommandMessage message = new CommandMessage();
					message.setOperation(CommandMessage.LOGIN_OPERATION);
					message.setMessageId(UUIDUtil.randomUUID());
					message.setBody(credentials);
					
					AsyncToken token = new AsyncToken(message);
					token.addResponder(new AsyncResponder() {

						@Override
						public void result(ResultEvent event) {
							authenticated = true;
							authenticating = false;
						}

						@Override
						public void fault(FaultEvent event) {
							authenticated = false;
							authenticating = false;
						}
					});
					tokens.add(0, token);
				}
				
				final Message[] messages = new Message[tokens.size()];
				try {
					for (int i = 0; i < messages.length; i++) {
						AsyncToken token = tokens.get(i);
						
						Message message = token.getMessage();
						
						if (id != null) {
							if (message.getHeaders() == null)
								message.setHeaders(new HashMap<String, Object>());
							message.setHeader(Message.ENDPOINT_HEADER, id);
						}
						
						messages[i] = message;
						activeTokens.putIfAbsent(message.getMessageId(), token);
					}

					AMF0Message amf0Message = createAMF0Message(messages);
					engine.send(connectedUri, amf0Message, new EngineResponseHandler() {
						
						@Override
						public void completed(AMF0Message message) {

							final int count = message.getBodyCount();
							for (int i = 0; i < count; i++) {
								AMF0Body body = message.getBody(i);
								AsyncMessage response = (AsyncMessage)body.getValue();
								String id = response.getCorrelationId();
								
								AsyncToken token = activeTokens.remove(id);
								if (token != null) {
									MessageEvent event = null;
									if (response instanceof ErrorMessage)
										event = new FaultEvent(token, (ErrorMessage)response);
									else
										event = new ResultEvent(token, response);
									token.callResponders(event);
								}
							}
						}
						
						@Override
						public void failed(Exception e) {
							callFaultActiveTokens(messages, "Connection.Failed", e);
						}
						
						@Override
						public void cancelled() {
							callFaultActiveTokens(messages, "Connection.Cancelled", null);
						}
					});
				}
				catch (Exception e) {
					callFaultActiveTokens(messages, "Client.Error", e);
					throw e;
				}
			}
		}
		catch (Exception e) {
			callPendingTokens("Channel.Failed", e);
			engine.getStatusHandler().handleException(new EngineException("Channel failed", e));
		}
		finally {
			connectionLock.unlock();
		}
	}
	
	private void callFaultActiveTokens(Message[] messages, String faultCode, Exception e) {
		for (Message message : messages) {
			if (message != null && message.getMessageId() != null) {
				AsyncToken token = activeTokens.remove(message.getMessageId());
				if (token != null) {
					ErrorMessage errorMessage = new ErrorMessage(message, e);
					errorMessage.setFaultCode(faultCode);
					errorMessage.setFaultString(e != null ? e.getMessage() : "");
					FaultEvent event = new FaultEvent(token, errorMessage);
					token.callResponders(event);
				}
			}
		}
	}
	
	protected void connect(final URI uri) {		
		if (connecting)
			return;
		
		connecting = true;

		final CommandMessage message = new CommandMessage();
		message.setOperation(CommandMessage.CLIENT_PING_OPERATION);
		message.setHeader("DSMessagingVersion", "1");
		message.setHeader("DSId", "nil");
		message.setMessageId(UUIDUtil.randomUUID());
		message.setBody(new HashMap<Object, Object>(0));
		
		AMF0Message amf0Message = createAMF0Message(message);
        
        engine.send(uri, amf0Message, new EngineResponseHandler() {
			
			@Override
			public void completed(AMF0Message message) {
				if (message.getBodyCount() == 1  && message.getBody(0).getValue() instanceof AcknowledgeMessage) {
					connectedUri = uri;
					internalSend();
				}
				connecting = false;
			}
			
			@Override
			public void failed(Exception e) {
				callPendingTokens("Connection.Failed", e);
				connecting = false;
			}
			
			@Override
			public void cancelled() {
				callPendingTokens("Connection.Cancelled", null);
				connecting = false;
			}
		});
	}
	
	private void callPendingTokens(String faultCode, Exception e) {
		for (AsyncToken token : pendingTokens) {
			ErrorMessage errorMessage = new ErrorMessage(token.getMessage(), e);
			errorMessage.setFaultCode(faultCode);
			errorMessage.setFaultString(e != null ? e.getMessage() : "");
			FaultEvent event = new FaultEvent(token, errorMessage);
			token.callResponders(event);
		}
		
		pendingTokens.clear();
	}
	
	protected AMF0Message createAMF0Message(Message message) {
		return createAMF0Message(new Message[]{message});
	}
	
	protected AMF0Message createAMF0Message(Message[] messages) {
		AMF0Message amf0Message = new AMF0Message();
		for (Message message : messages) {
			AMF3Object data = new AMF3Object(message);
	        AMF0Body body = new AMF0Body("", "/" + (index++), new Object[]{data}, AMF0Body.DATA_TYPE_AMF3_OBJECT);
	        amf0Message.addBody(body);
		}
		return amf0Message;
	}
}
