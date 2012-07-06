package org.granite.websocket.client.test;

import java.net.URI;

import org.granite.client.messaging.Producer;
import org.granite.client.messaging.WebSocketChannel;
import org.granite.client.messaging.engine.JettyWebSocketEngine;
import org.granite.client.messaging.engine.WebSocketEngine;
import org.junit.Test;

import flex.messaging.messages.AsyncMessage;

public class TestSendMessage {

	@Test
	public void testSendMessage() throws Exception {
		WebSocketEngine engine = new JettyWebSocketEngine();		
		WebSocketChannel channel = new WebSocketChannel(engine, null, new URI("ws://localhost:8080/shop-admin/gravityamf/amf"));
		
		try {
			engine.start();
			
			Producer producer = new Producer(channel, "wineshopTopic");
			producer.setTopic("tideDataTopic");
			AsyncMessage message = new AsyncMessage();
			message.setBody("Fuck");
			producer.send(message);
			
			Thread.sleep(20000);
		}
		finally {
			engine.stop();
		}
	}
}
