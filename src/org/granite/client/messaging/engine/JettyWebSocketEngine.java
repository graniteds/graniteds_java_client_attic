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

package org.granite.client.messaging.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class JettyWebSocketEngine extends AbstractEngine implements WebSocketEngine {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketEngine.class);

	private WebSocketClientFactory webSocketClientFactory = null;
	protected CookieStore cookieStore = new BasicCookieStore();
	
	
	@Override
	public synchronized void start() {
		super.start();
		
		log.info("Starting Jetty WebSocket engine");
		
		try {
			webSocketClientFactory = new WebSocketClientFactory();
			webSocketClientFactory.setBufferSize(4096);
			webSocketClientFactory.start();
			
			final long timeout = System.currentTimeMillis() + 10000L; // 10sec.
			while (!webSocketClientFactory.isStarted()) {
				if (System.currentTimeMillis() > timeout)
					throw new TimeoutException("Jetty WebSocketFactory start process too long");
				Thread.sleep(100);
			}
		}
		catch (Exception e) {
			super.stop();
			
			webSocketClientFactory = null;
			
			statusHandler.handleException(new EngineException("Could not start Jetty WebSocketFactory", e));
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return (super.isStarted() && webSocketClientFactory != null && webSocketClientFactory.isStarted());
	}
	
	@Override
	public void connect(final URI uri, EngineMessageHandler handler, String clientId, String sessionId) {
	    if (!isStarted()) {
			statusHandler.handleException(new EngineException("Jetty WebSocket engine not started"));
			return;
		}
	    
		WebSocket webSocket = new WebSocketImpl(handler);
		
		try {
			String u = uri.toString();
			if (clientId != null)
				u += "?GDSClientId=" + clientId;
			if (sessionId != null)
				u += (clientId != null ? "&" : "?") + "GDSSessionId=" + sessionId;
			
			log.info("Connecting channel clientId %s sessionId %s", clientId, sessionId);
			
			WebSocketClient webSocketClient = webSocketClientFactory.newWebSocketClient();
			webSocketClient.setMaxIdleTime(maxIdleTime);
			webSocketClient.setMaxTextMessageSize(1024);
			webSocketClient.setProtocol("gravity");
			webSocketClient.open(new URI(u), webSocket);
		}
		catch (Exception e) {
			statusHandler.handleException(new EngineException("Could not connect to uri " + uri, e));
		}
	}
	
	public class WebSocketImpl implements WebSocket, OnBinaryMessage {
		
		private final EngineMessageHandler handler;
		
		public WebSocketImpl(EngineMessageHandler handler) {
			this.handler = handler;
		}

		@Override
		public void onMessage(byte[] data, int offset, int length) {
			SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>(0));
			ByteArrayInputStream is = new ByteArrayInputStream(data, offset, length);			
			AMF3Deserializer deserializer = new AMF3Deserializer(is);
			try {
				Object[] messages = (Object[])deserializer.readObject();
				for (Object message : messages)
					handler.onMessage((Message)message);
			} 
			catch (IOException e) {
				statusHandler.handleException(new EngineException("Could not read message", e));
			}
			finally {
				GraniteContext.release();
			}
		}

		@Override
		public void onOpen(Connection connection) {
			handler.onConnect(new WebSocketConnectionImpl(connection));
		}

		@Override
		public void onClose(int closeCode, String message) {
			handler.onDisconnect(closeCode, message);
		}
		
		public class WebSocketConnectionImpl implements WebSocketEngine.Connection {
			
			private final Connection connection;
			
			public WebSocketConnectionImpl(Connection connection) {
				this.connection = connection;
			}

			@Override
			public void send(Message[] messages) {
				ByteArrayOutputStream os = new PublicByteArrayOutputStream(1000);			
				SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>(0));
				AMF3Serializer serializer = new AMF3Serializer(os);
				try {
					serializer.writeObject(messages);
					
					byte[] data = os.toByteArray();
					connection.sendMessage(data, 0, data.length);
				} 
				catch (IOException e) {
					statusHandler.handleException(new EngineException("Could not send message", e));
				}
				finally {
					GraniteContext.release();
				}
			}
		}
	}


	@Override
	public synchronized void stop() {
		super.stop();
		
		try {
			webSocketClientFactory.stop();
		}
		catch (Exception e) {
			statusHandler.handleException(new EngineException("Could not stop Jetty WebSocketFactory", e));
		}
		finally {
			webSocketClientFactory = null;
		}
	}
}
