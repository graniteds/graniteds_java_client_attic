package org.granite.tide.data;

import java.util.ArrayList;
import java.util.List;

import org.granite.logging.Logger;
import org.granite.messaging.Consumer;
import org.granite.messaging.Consumer.SubscriptionListener;
import org.granite.messaging.MessageListener;
import org.granite.tide.Context;
import org.granite.tide.ContextAware;
import org.granite.tide.data.EntityManager.UpdateKind;
import org.granite.tide.rpc.ServerSession;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

public class DataObserver implements ContextAware {
    
    private static Logger log = Logger.getLogger(DataObserver.class);

    private Context context;
    private ServerSession serverSession = null;
    private EntityManager entityManager = null;
    private String destination = null;
    
	private Consumer consumer = null;

	
	public DataObserver(String destination, ServerSession serverSession, EntityManager entityManager) {
		this.destination = destination;
		this.serverSession = serverSession;
		this.entityManager = entityManager;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void start() {
        consumer = serverSession.getConsumer(destination);
		consumer.setSubscriptionListener(new SubscriptionListenerImpl());
        consumer.setTopic("tideDataTopic");
	}	
	
	public void stop() {
		if (consumer.isSubscribed())
			unsubscribe();
	}
	
	public void setTopic(String topic) {
		consumer.setTopic(topic);
	}
	
	
	/**
	 * 	Subscribe the data topic
	 */
	public void subscribe() {
		consumer.setMessageListener(new MessageListenerImpl());
	    consumer.subscribe();
	    serverSession.checkWaitForLogout();
	}
	
	public void unsubscribe() {
		consumer.unsubscribe();
		consumer.setMessageListener(null);
	    serverSession.checkWaitForLogout();
	}
	
	
	private class SubscriptionListenerImpl implements SubscriptionListener {
		@Override
		public void onSubscribeSuccess(AcknowledgeMessage ackMessage, CommandMessage subscriptionMessage) {
			log.info("Destination %s subscribed", destination);
		}

		@Override
		public void onSubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message) {
			log.error("Destination %s could not be subscribed: %s", destination, faultCode);
		}

		@Override
		public void onUnsubscribeSuccess(AcknowledgeMessage message, CommandMessage unsubscriptionMessage) {
			log.info("Destination %s unsubscribed", destination);
			// consumer.disconnect();
			serverSession.tryLogout();
		}

		@Override
		public void onUnsubscribeFault(String faultCode, String faultString, String faultDetail, ErrorMessage message) {
			log.error("Destination %s could not be unsubscribed: %s", destination, faultCode);
			// consumer.disconnect();
			serverSession.tryLogout();
		}
	}

	/**
	 * 	Message handler that merges data from the JMS topic in the current context.<br/>
	 *  Could be overriden to provide custom behaviour.
	 * 
	 *  @param event message event from the Consumer
	 */
    public class MessageListenerImpl implements MessageListener {
		@Override
		public void onMessage(Message msg) {
	        log.debug("Destination %s message received %s", destination, msg.toString());
	        
	        final Message message = msg;
	        
	        context.callLater(new Runnable() {
				@Override
				public void run() {
			        String receivedSessionId = (String)message.getHeader("GDSSessionID");
			        if (receivedSessionId != null && receivedSessionId.equals(serverSession.getSessionId()))
			        	receivedSessionId = null;
			        
			        try {
			        	MergeContext mergeContext = entityManager.initMerge();
			        	
				        Object[] updates = (Object[])message.getBody();
				        List<EntityManager.Update> upds = new ArrayList<EntityManager.Update>();
				        for (Object update : updates)
				        	upds.add(new EntityManager.Update(UpdateKind.forName(((Object[])update)[0].toString().toUpperCase()), ((Object[])update)[1]));
				        
			        	entityManager.handleUpdates(mergeContext, receivedSessionId, upds);
			        	entityManager.raiseUpdateEvents(context, upds);
			        }
			        finally {
			        	MergeContext.destroy(entityManager);
			        }
				}
	        });
		}
    }
}
