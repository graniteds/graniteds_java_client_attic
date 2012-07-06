package org.granite.websocket.client.test;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.junit.Test;

public class TestReceiveMessage {

	@Test
	public void testReceiveMessage() throws Exception {
		HTTPTransport transport = new ApacheAsyncTransport();		
		AMFMessagingChannel channel = new AMFMessagingChannel(transport, "<id>", new URI("http://localhost:8080/shop-admin/gravityamf/amf"));
		
		transport.start();
		try {
			Consumer consumer = new Consumer(channel, "wineshopTopic", "tideDataTopic");
			consumer.addMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					if (message instanceof ObjectMessage) try {
						System.out.println(((ObjectMessage)message).getObject());
					}
					catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
			
			ResponseMessageFuture future = consumer.subscribe(new ResultFaultIssuesResponseListener() {

				@Override
				public void onResult(ResultEvent event) {
					System.out.println("onSubscribeSuccess");
				}

				@Override
				public void onFault(FaultEvent event) {
					System.out.println("onSubscribeFault");
				}

				@Override
				public void onIssue(IssueEvent event) {
					System.out.println("onSubscribeIssue");
				}
			});
			
			future.get();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (TimeoutException e) {
			e.printStackTrace();
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		finally {
			transport.stop();
		}
	}
}
