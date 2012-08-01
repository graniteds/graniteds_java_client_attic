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

package org.granite.client.messaging.transport.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.WebSocketTransport;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;

/**
 * @author Franck WOLFF
 */
public class JettyWebSocketTransport extends AbstractTransport implements WebSocketTransport {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketTransport.class);

	private final static int CLOSE_NORMAL = 1000;
//	private final static int CLOSE_SHUTDOWN = 1001;
//	private final static int CLOSE_PROTOCOL = 1002;
	
	private WebSocketClientFactory webSocketClientFactory = null;
	
	private int maxIdleTime = 30000;
	
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
	@Override
	public synchronized boolean start() {
		if (webSocketClientFactory != null && webSocketClientFactory.isStarted())
			return true;
		
		stop();

		log.info("Starting Jetty WebSocketClient engine...");
		
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
			
			log.info("Jetty WebSocketClient engine started.");
			return true;
		}
		catch (Exception e) {
			webSocketClientFactory = null;
			getStatusHandler().handleException(new TransportException("Could not start Jetty WebSocketFactory", e));
			
			log.error(e, "Jetty WebSocketClient engine failed to start.");
			return false;
		}
	}
	
	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) {

		synchronized (channel) {

			ChannelData channelData = channel.getTransportData();
			if (channelData == null) {
				channelData = new ChannelData();
				channel.setTransportData(channelData);
			}
			
			if (message != null)
				channelData.pendingMessages.addLast(message);
			
			if (channelData.connection == null) {
				connect(channel, message);
				return null;
			}

			while (!channelData.pendingMessages.isEmpty()) {
				TransportMessage pendingMessage = channelData.pendingMessages.removeFirst();
				try {
					PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(256);
					pendingMessage.encode(os);
					byte[] data = os.getBytes();
					channelData.connection.sendMessage(data, 0, os.size());
				}
				catch (IOException e) {
					channelData.pendingMessages.addFirst(pendingMessage);
					// report error...
					break;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void poll(final Channel channel, final TransportMessage message) {
		send(channel, message);
	}
	
	public Future<Connection> connect(final Channel channel, final TransportMessage engineMessage) {
		URI uri = channel.getUri();
		
		try {		
			WebSocketClient webSocketClient = webSocketClientFactory.newWebSocketClient();
			webSocketClient.setMaxIdleTime(maxIdleTime);
			webSocketClient.setMaxTextMessageSize(1024);
			webSocketClient.setProtocol("gravity");
			
			return webSocketClient.open(uri, new OnBinaryMessage() {
				
				@Override
				public void onOpen(Connection connection) {
					synchronized (channel) {
						((ChannelData)channel.getTransportData()).connection = connection;
						send(channel, null);
					}
				}
	
				@Override
				public void onMessage(byte[] data, int offset, int length) {
					channel.onMessage(new ByteArrayInputStream(data, offset, length));
				}
	
				@Override
				public void onClose(int closeCode, String message) {
					synchronized (channel) {
						((ChannelData)channel.getTransportData()).connection = null;
						if (closeCode != CLOSE_NORMAL)
							channel.onError(engineMessage, new RuntimeException(message + " (code=" + closeCode + ")"));
					}
				}
			});
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not connect to uri " + channel.getUri(), e));
			
			return null;
		}
	}
	
	private static class ChannelData {
		
		private final LinkedList<TransportMessage> pendingMessages = new LinkedList<TransportMessage>();
		private Connection connection = null;
	}

	@Override
	public synchronized void stop() {
		if (webSocketClientFactory == null)
			return;
		
		log.info("Stopping Jetty WebSocketClient engine...");
		
		super.stop();
		
		try {
			webSocketClientFactory.stop();
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not stop Jetty WebSocketFactory", e));

			log.error(e, "Jetty WebSocketClient failed to stop properly.");
		}
		finally {
			webSocketClientFactory = null;
		}
		
		log.info("Jetty WebSocketClient engine stopped.");
	}
}
