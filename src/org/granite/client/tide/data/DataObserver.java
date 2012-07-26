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
import org.granite.client.messaging.messages.requests.SubscribeMessage;
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
		consumer.addMessageListener(new MessageListenerImpl());
	    consumer.subscribe(new SubscriptionListenerImpl());
	    serverSession.checkWaitForLogout();
	}
	
	public void unsubscribe() {
		consumer.unsubscribe();
		consumer.removeMessageListener(messageListener);
	    serverSession.checkWaitForLogout();
	}
	
	
	private class SubscriptionListenerImpl extends ResultFaultIssuesResponseListener {
		@Override
		public void onResult(ResultEvent event) {
			if (event.getRequest() instanceof SubscribeMessage)
				log.info("Destination %s subscribed", destination);
			else {
				log.info("Destination %s unsubscribed", destination);
				serverSession.tryLogout();
			}
		}

		@Override
		public void onFault(FaultEvent event) {
			if (event.getRequest() instanceof SubscribeMessage)
				log.error("Destination %s could not be subscribed: %s", destination, event.getCode());
			else {
				log.error("Destination %s could not be unsubscribed: %s", destination, event.getCode());
				serverSession.tryLogout();
			}
		}

		@Override
		public void onIssue(IssueEvent event) {
			if (event.getRequest() instanceof SubscribeMessage)
				log.error("Destination %s could not be subscribed: %s", destination, event.getType());
			else {
				log.error("Destination %s could not be unsubscribed: %s", destination, event.getType());
				serverSession.tryLogout();
			}
		}
	}

	
	private MessageListener messageListener = new MessageListenerImpl();
	
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
			        try {
				        String receivedSessionId = (String)message.getStringProperty("GDSSessionID");
				        if (receivedSessionId != null && receivedSessionId.equals(serverSession.getSessionId()))
				        	receivedSessionId = null;
				        
			        	MergeContext mergeContext = entityManager.initMerge();
			        	
				        Object[] updates = (Object[])message.getObject();
				        List<EntityManager.Update> upds = new ArrayList<EntityManager.Update>();
				        for (Object update : updates)
				        	upds.add(new EntityManager.Update(UpdateKind.forName(((Object[])update)[0].toString().toUpperCase()), ((Object[])update)[1]));
				        
			        	entityManager.handleUpdates(mergeContext, receivedSessionId, upds);
			        	entityManager.raiseUpdateEvents(context, upds);
			        }
			        catch (JMSException e) {
			        	// Ignored: should never happen
			        }
			        finally {
			        	MergeContext.destroy(entityManager);
			        }
				}
	        });
		}
    }
}
