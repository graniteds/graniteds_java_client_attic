package org.granite.client.tide.data;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;

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
        consumer = serverSession.getConsumer(destination, "tideDataTopic");
        consumer.addMessageListener(new MessageListenerImpl());
	}	
	
	public void stop() {
		if (consumer.isSubscribed())
			unsubscribe();
	}
	
//	public void setTopic(String topic) {
//		consumer.setTopic(topic);
//	}
	
	
	/**
	 * 	Subscribe the data topic
	 */
	public void subscribe() {
	    consumer.subscribe(new SubscriptionListenerImpl());
	    serverSession.checkWaitForLogout();
	}
	
	public void unsubscribe() {
		consumer.unsubscribe(new UnsubscriptionListenerImpl());
	    serverSession.checkWaitForLogout();
	}
	
	private class UnsubscriptionListenerImpl extends ResultFaultIssuesResponseListener {
		
		@Override
		public void onResult(ResultEvent event) {
			log.info("Destination %s unsubscribed", destination);
			// consumer.disconnect();
			serverSession.tryLogout();
		}

		@Override
		public void onFault(FaultEvent event) {
			log.error("Destination %s could not be unsubscribed: %s", destination, event.getCode());
			// consumer.disconnect();
			serverSession.tryLogout();
		}

		@Override
		public void onIssue(IssueEvent event) {
			log.error("Destination %s could not be unsubscribed: %s", destination, event);
			// consumer.disconnect();
			serverSession.tryLogout();
		}
	}

	
	private class SubscriptionListenerImpl extends ResultFaultIssuesResponseListener {

		@Override
		public void onResult(ResultEvent event) {
			log.info("Destination %s subscribed", destination);
		}

		@Override
		public void onFault(FaultEvent event) {
			log.error("Destination %s could not be subscribed: %s", destination, event.getCode());
		}

		@Override
		public void onIssue(IssueEvent event) {
			log.error("Destination %s could not be subscribed: %s", destination, event);
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
	        
	        final ObjectMessage message = (ObjectMessage)msg;
	        
	        context.callLater(new Runnable() {
				@Override
				public void run() {
//			        String receivedSessionId = (String)message.getHeader("GDSSessionID");
//			        if (receivedSessionId != null && receivedSessionId.equals(serverSession.getSessionId()))
//			        	receivedSessionId = null;
			        
			        try {
			        	MergeContext mergeContext = entityManager.initMerge();
			        	
				        Object[] updates = (Object[])message.getObject();
				        List<EntityManager.Update> upds = new ArrayList<EntityManager.Update>();
				        for (Object update : updates)
				        	upds.add(new EntityManager.Update(UpdateKind.forName(((Object[])update)[0].toString().toUpperCase()), ((Object[])update)[1]));
				        
			        	entityManager.handleUpdates(mergeContext, null, upds);
			        	entityManager.raiseUpdateEvents(context, upds);
			        }
			        catch (JMSException e) {
			        	
			        }
			        finally {
			        	MergeContext.destroy(entityManager);
			        }
				}
	        });
		}
    }
}
