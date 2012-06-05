package org.granite.websocket.client.test;

import java.net.URI;

import org.granite.messaging.Consumer;
import org.granite.messaging.Consumer.SubscriptionListener;
import org.granite.messaging.MessageListener;
import org.granite.messaging.WebSocketChannel;
import org.granite.messaging.engine.JettyWebSocketEngine;
import org.granite.messaging.engine.WebSocketEngine;
import org.junit.Test;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

public class TestReceiveMessage {

	@Test
	public void testReceiveMessage() throws Exception {
		WebSocketEngine engine = new JettyWebSocketEngine();		
		WebSocketChannel channel = new WebSocketChannel(engine, null, new URI("ws://localhost:8080/shop-admin/gravityamf/amf"));
		
		try {
			engine.start();
			
			Consumer consumer = new Consumer(channel, "wineshopTopic");
			consumer.setTopic("tideDataTopic");
			consumer.setSubscriptionListener(new SubscriptionListener() {
				@Override
				public void onUnsubscribeSuccess(AcknowledgeMessage message, CommandMessage unsubscriptionMessage) {
					System.out.println("onUnubscribeSuccess");
				}
				
				@Override
				public void onUnsubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message) {
					System.out.println("onUnsubscribeSuccess");
				}
				
				@Override
				public void onSubscribeSuccess(AcknowledgeMessage ackMessage, CommandMessage subscriptionMessage) {
					System.out.println("onSubscribeSuccess");
				}
				
				@Override
				public void onSubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message) {
					System.out.println("onSubscribeFault");
				}
			});
			consumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					System.out.println(message.getBody());
				}
			});
			consumer.subscribe();
			
			Thread.sleep(200000);
		}
		finally {
			engine.stop();
		}
	}
}
