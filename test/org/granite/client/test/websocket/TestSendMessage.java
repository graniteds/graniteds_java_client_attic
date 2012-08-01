package org.granite.websocket.client.test;

import java.net.URI;

import org.granite.client.messaging.Producer;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.junit.Test;

public class TestSendMessage {

	@Test
	public void testSendMessage() throws Exception {
		HTTPTransport transport = new ApacheAsyncTransport();		
		AMFMessagingChannel channel = new AMFMessagingChannel(transport, "<id>", new URI("http://localhost:8080/shop-admin/gravityamf/amf"));
		
		transport.start();
		try {
			Producer producer = new Producer(channel, "wineshopTopic", "tideDataTopic");
			producer.publish("Hello world").get();
		}
		finally {
			transport.stop();
		}
	}
}
