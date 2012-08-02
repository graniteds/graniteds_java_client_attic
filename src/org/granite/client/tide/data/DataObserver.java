package org.granite.client.tide.data;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.TopicMessageListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.push.TopicMessage;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;


public class DataObserver implements ContextAware {
    
    private static Logger log = Logger.getLogger(DataObserver.class);
    
    public static final String DATA_OBSERVER_TOPIC_NAME = "tideDataTopic";

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
        consumer = serverSession.getConsumer(destination, DATA_OBSERVER_TOPIC_NAME);
	}	
	
	public void stop() {
		if (consumer.isSubscribed())
			unsubscribe();
	}
	
	
	/**
	 * 	Subscribe the data topic
	 */
	public void subscribe() {
		consumer.addMessageListener(messageListener);
	    consumer.subscribe(subscriptionListener);
	    serverSession.checkWaitForLogout();
	}
	
	public void unsubscribe() {
		if (consumer.isSubscribed()) {
			consumer.removeMessageListener(messageListener);
			consumer.unsubscribe(unsubscriptionListener);
		    serverSession.checkWaitForLogout();
		}
	}
	
	private ResponseListener subscriptionListener = new SubscriptionListenerImpl(); 
	private ResponseListener unsubscriptionListener = new UnsubscriptionListenerImpl(); 
	
	private class SubscriptionListenerImpl extends ResultFaultIssuesResponseListener {
		@Override
		public void onResult(ResultEvent event) {
			log.info("Destination %s subscribed", destination);
			
			serverSession.tryLogout();
		}

		@Override
		public void onFault(FaultEvent event) {
			log.error("Destination %s could not be subscribed: %s", destination, event.getCode());
			
			serverSession.tryLogout();
		}

		@Override
		public void onIssue(IssueEvent event) {
			log.error("Destination %s could not be subscribed: %s", destination, event.getType());
			
			serverSession.tryLogout();
		}
	}
	
	private class UnsubscriptionListenerImpl extends ResultFaultIssuesResponseListener {
		@Override
		public void onResult(ResultEvent event) {
			log.info("Destination %s unsubscribed", destination);
			
			serverSession.tryLogout();
		}

		@Override
		public void onFault(FaultEvent event) {
			log.error("Destination %s could not be unsubscribed: %s", destination, event.getCode());
			
			serverSession.tryLogout();
		}

		@Override
		public void onIssue(IssueEvent event) {
			log.error("Destination %s could not be unsubscribed: %s", destination, event.getType());
			
			serverSession.tryLogout();
		}
	}

	
	private TopicMessageListener messageListener = new TopicMessageListenerImpl();
	
	/**
	 * 	Message handler that merges data from the JMS topic in the current context.<br/>
	 *  Could be overriden to provide custom behaviour.
	 * 
	 *  @param event message event from the Consumer
	 */
    public class TopicMessageListenerImpl implements TopicMessageListener {
		@Override
		public void onMessage(TopicMessageEvent event) {
	        log.debug("Destination %s message event received %s", destination, event.toString());
	        
	        final TopicMessage message = event.getMessage();
	        
	        context.callLater(new Runnable() {
				@Override
				public void run() {
			        try {
				        String receivedSessionId = (String)message.getHeader("GDSSessionID");
				        if (receivedSessionId != null && receivedSessionId.equals(serverSession.getSessionId()))
				        	receivedSessionId = null;
				        
			        	MergeContext mergeContext = entityManager.initMerge();
			        	
				        Object[] updates = (Object[])message.getData();
				        List<EntityManager.Update> upds = new ArrayList<EntityManager.Update>();
				        for (Object update : updates)
				        	upds.add(new EntityManager.Update(UpdateKind.forName(((Object[])update)[0].toString().toUpperCase()), ((Object[])update)[1]));
				        
			        	entityManager.handleUpdates(mergeContext, receivedSessionId, upds);
			        	entityManager.raiseUpdateEvents(context, upds);
			        }
			        catch (Exception e) {
			        	log.error(e, "Error during received message processing");
			        }
			        finally {
			        	MergeContext.destroy(entityManager);
			        }
				}
	        });
		}
    }
}
