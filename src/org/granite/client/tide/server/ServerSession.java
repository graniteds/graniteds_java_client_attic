package org.granite.client.tide.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.RemoteClassScanner;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.TopicAgent;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.SessionAwareChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.client.messaging.transport.jetty.JettyWebSocketTransport;
import org.granite.client.tide.BeanManager;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.PlatformConfigurable;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.Update;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.logging.Logger;
import org.granite.tide.Expression;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationResult;


@PlatformConfigurable
public class ServerSession implements ContextAware {

	private static Logger log = Logger.getLogger(ServerSession.class);
	
	public static final String SESSION_ID_TAG = "org.granite.sessionId";
    public static final String CONVERSATION_TAG = "conversationId";
    public static final String CONVERSATION_PROPAGATION_TAG = "conversationPropagation";
    public static final String IS_LONG_RUNNING_CONVERSATION_TAG = "isLongRunningConversation";
    public static final String WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG = "wasLongRunningConversationEnded";
    public static final String WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG = "wasLongRunningConversationCreated";
    public static final String IS_FIRST_CALL_TAG = "org.granite.client.tide.isFirstCall";
    public static final String IS_FIRST_CONVERSATION_CALL_TAG = "org.granite.client.tide.isFirstConversationCall";
	
    public static final String CONTEXT_RESULT = "org.granite.tide.result";
    public static final String CONTEXT_FAULT = "org.granite.tide.fault";
    
	public static final String LOGIN = "org.granite.client.tide.login";
	public static final String LOGOUT = "org.granite.client.tide.logout";
	public static final String SESSION_EXPIRED = "org.granite.client.tide.sessionExpired";
	
	private static final String DEFAULT_REMOTING_URL_MAPPING = "/graniteamf/amf.txt";
	private static final String DEFAULT_COMET_URL_MAPPING = "/gravityamf/amf.txt";
	private static final String DEFAULT_WEBSOCKET_URL_MAPPING = "/websocketamf/amf";
    
    
	@SuppressWarnings("unused")
	private boolean confChanged = false;
	private boolean useWebSocket = true;
    private Transport remotingTransport = null;
    private Transport messagingTransport = null;
    private String protocol = "http";
    private String contextRoot = "";
    private String serverName = null;
    private int serverPort = 0;
    private String graniteUrlMapping = DEFAULT_REMOTING_URL_MAPPING;		// .txt for stupid bug in IE8
    private String gravityUrlMapping = DEFAULT_COMET_URL_MAPPING;
    
    private URI graniteURI;
	private URI gravityURI;
    
	private Context context = null;
    private TrackingContext trackingContext = new TrackingContext();
	
	private Status status = new DefaultStatus();
	
	private String sessionId = null;
	private boolean isFirstCall = true;
	
	private LogoutState logoutState = new LogoutState();
		
	private String destination = null;
	private Configuration configuration = null;
    private RemotingChannel remotingChannel;
	private MessagingChannel messagingChannel;
	protected Map<String, RemoteService> remoteServices = new HashMap<String, RemoteService>();
	protected Map<String, TopicAgent> topicAgents = new HashMap<String, TopicAgent>();
	private RemoteClassScanner remoteClassConfigurator = new RemoteClassScanner();
	
	
    public ServerSession() throws Exception {    	
    }
    
    public ServerSession(String destination, String contextRoot, String serverName, int serverPort) throws Exception {
    	this(destination, "http", contextRoot, serverName, serverPort, null, null);
    }

    public ServerSession(String destination, String contextRoot, String serverName, int serverPort, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
    	this(destination, "http", contextRoot, serverName, serverPort, graniteUrlMapping, gravityUrlMapping);
    }
    
    public ServerSession(String destination, String protocol, String contextRoot, String serverName, int serverPort, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
        super();
        this.destination = destination;
        this.protocol = protocol;
        this.contextRoot = contextRoot;
        this.serverName = serverName;
        this.serverPort = serverPort;
        if (graniteUrlMapping != null)
        	this.graniteUrlMapping = graniteUrlMapping;
        if (gravityUrlMapping != null)
        	this.gravityUrlMapping = gravityUrlMapping;
    }

    	
    public void setContextRoot(String contextRoot) {
    	this.contextRoot = contextRoot;
    	confChanged = true;
    }
    
    public void setProtocol(String protocol) {
    	this.protocol = protocol;
    	confChanged = true;
    }
    
    public void setServerName(String serverName) {
    	this.serverName = serverName;
    	confChanged = true;
    }
    
    public void setServerPort(int serverPort) {
    	this.serverPort = serverPort;
    	confChanged = true;
    }
    
    public void setGraniteUrlMapping(String graniteUrlMapping) {
    	this.graniteUrlMapping = graniteUrlMapping;
    	confChanged = true;
    }
    
    public void setGravityUrlMapping(String gravityUrlMapping) {
    	this.gravityUrlMapping = gravityUrlMapping;
    	confChanged = true;
    }
    
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public TrackingContext getTrackingContext() {
		return trackingContext;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setUseWebSocket(boolean useWebSocket) {
		this.useWebSocket = useWebSocket;
		if (useWebSocket && DEFAULT_COMET_URL_MAPPING.equals(gravityUrlMapping))
			this.gravityUrlMapping = DEFAULT_WEBSOCKET_URL_MAPPING;
	}
	
	public void setRemotingTransport(Transport transport) {
		this.remotingTransport = transport;
	}
	
	public void setMessagingTransport(Transport transport) {
		this.messagingTransport = transport;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public void addRemoteClassPackage(String packageName) {		
		remoteClassConfigurator.addPackageName(packageName);
	}
	
	public void setRemoteClassPackage(Set<String> packageNames) {
		remoteClassConfigurator.setPackageNames(packageNames);
	}
	
	public void start() throws Exception {
		if (remotingTransport == null)
			remotingTransport = new ApacheAsyncTransport();
		
		if (messagingTransport == null)
			messagingTransport = useWebSocket ? new JettyWebSocketTransport() : remotingTransport;
		
		remotingTransport.setStatusHandler(statusHandler);
		remotingTransport.start();
		if (messagingTransport != remotingTransport) {
			messagingTransport.setStatusHandler(statusHandler);
			messagingTransport.start();
		}
		
		configuration.addConfigurator(remoteClassConfigurator);
		configuration.load();
		
		graniteURI = new URI(protocol + "://" + this.serverName + (this.serverPort > 0 ? ":" + this.serverPort : "") + this.contextRoot + this.graniteUrlMapping);
		remotingChannel = new AMFRemotingChannel(remotingTransport, configuration, "graniteamf", graniteURI, 1);

		if (useWebSocket)
			gravityURI = new URI(protocol.replace("http", "ws") + "://" + this.serverName + (this.serverPort > 0 ? ":" + this.serverPort : "") + this.contextRoot + this.gravityUrlMapping);
		else
			gravityURI = new URI(protocol + "://" + this.serverName + (this.serverPort > 0 ? ":" + this.serverPort : "") + this.contextRoot + this.gravityUrlMapping);
		messagingChannel = new AMFMessagingChannel(messagingTransport, configuration, "gravityamf", gravityURI);
	}
	
	public void stop()throws Exception {
		if (remotingTransport != null)
			remotingTransport.stop();		
		remotingChannel = null;
		
		if (messagingTransport != null && messagingTransport != remotingTransport)
			messagingTransport.stop();		
		messagingChannel = null;
	}
	
	
	public static interface ServiceFactory {
		
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination);
		
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic);
		
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic);
	}
	
	private static class DefaultServiceFactory implements ServiceFactory {
		
		@Override
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination) {
			return new RemoteService(remotingChannel, destination);
		}
		
		@Override
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Producer(messagingChannel, destination, topic);
		}
		
		@Override
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Consumer(messagingChannel, destination, topic);
		}
	}
	
	private ServiceFactory serviceFactory = new DefaultServiceFactory();
	
	public void setServiceFactory(ServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}
	
	public RemoteService getRemoteService() {
		return getRemoteService(destination);
	}
	public synchronized RemoteService getRemoteService(String destination) {
		if (remotingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		RemoteService remoteService = remoteServices.get(destination);
		if (remoteService == null) {
			remoteService = serviceFactory.newRemoteService(remotingChannel, destination);
			remoteServices.put(destination, remoteService);
		}
		return remoteService;
	}

	public synchronized Consumer getConsumer(String destination, String topic) {
		if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		String key = destination + '@' + topic;
		TopicAgent consumer = topicAgents.get(key);
		if (consumer == null) {
			consumer = serviceFactory.newConsumer(messagingChannel, destination, topic);
			topicAgents.put(key, consumer);
		}
		return consumer instanceof Consumer ? (Consumer)consumer : null;
	}

	public synchronized Producer getProducer(String destination, String topic) {
		if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		String key = destination + '@' + topic;
		TopicAgent producer = topicAgents.get(key);
		if (producer == null) {
			producer = serviceFactory.newProducer(messagingChannel, destination, topic);
			topicAgents.put(key, producer);
		}
		return producer instanceof Producer ? (Producer)producer : null;
	}
	
	public boolean isFirstCall() {
		return isFirstCall;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public boolean isLogoutInProgress() {
		return logoutState.logoutInProgress;
	}
	
	public void trackCall() {
		isFirstCall = false;
	}
	
	public void handleResultEvent(Event event) {
		if (event instanceof ResultEvent)
			sessionId = (String)((ResultEvent)event).getMessage().getHeader(SESSION_ID_TAG);
		else if (event instanceof IncomingMessageEvent<?>)
			sessionId = (String)((IncomingMessageEvent<?>)event).getMessage().getHeader(SESSION_ID_TAG);

		if (messagingChannel != null && sessionId != null && messagingChannel instanceof SessionAwareChannel)
			((SessionAwareChannel)messagingChannel).setSessionId(sessionId);
		isFirstCall = false;
		status.setConnected(true);
	}
	
	public void handleFaultEvent(FaultEvent event, FaultMessage emsg) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);

		if (messagingChannel != null && sessionId != null && messagingChannel instanceof SessionAwareChannel)
			((SessionAwareChannel)messagingChannel).setSessionId(sessionId);
		
        if (emsg != null && emsg.getCode().equals(Code.SERVER_CALL_FAILED))
        	status.setConnected(false);            
	}
	
	private final TransportStatusHandler statusHandler = new TransportStatusHandler() {
		
		private int busyCount = 0;
		
		@Override
		public void handleIO(boolean active) {			
			if (active)
				busyCount++;
			else
				busyCount--;
			status.setBusy(busyCount > 0);
		}

		@Override
		public void handleException(TransportException e) {
			// TODO: never called
			log.error(e, "Transport failed");
		}		
	};
	
	
    /**
     *  @private
     *  Implementation of component invocation
     *  
     *  @param component component proxy
     *  @param op remote operation
     *  @param args array of operation arguments
     *  @param responder Tide responder
     *  @param withContext send additional context with the call
     *  @param handler optional operation handler
     * 
     *  @return token for the remote operation
     */
	
    
    public void login(String username, String password) {
    	remotingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
    	messagingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
    }
    
    public void afterLogin() {
		log.info("Application session authenticated");
		
		context.getEventBus().raiseEvent(context, LOGIN);
    }
    
    public void sessionExpired() {
		log.info("Application session expired");
		
		sessionId = null;
		isFirstCall = true;
		
		logoutState.sessionExpired();
		
		context.getEventBus().raiseEvent(context, SESSION_EXPIRED);		
		context.getEventBus().raiseEvent(context, LOGOUT);		
    }
    
	/**
	 * 	@private
	 * 	Implementation of logout
	 * 	
	 * 	@param ctx current context
	 *  @param componentName component name of identity
	 */
	public void logout(final Observer logoutObserver) {
		logoutState.logout(logoutObserver, new TimerTask() {
			@Override
			public void run() {
				log.info("Force session logout");
				logoutState.logout(logoutObserver);
				tryLogout();
			}
		});
		
		context.getEventBus().raiseEvent(context, LOGOUT);
		
        tryLogout();
    }
	
	/**
	 * 	Notify the framework that it should wait for a async operation before effectively logging out.
	 *  Only if a logout has been requested.
	 */
	public void checkWaitForLogout() {
		isFirstCall = false;
		
		logoutState.checkWait();
	}
	
	/**
	 * 	Try logout. Should be called after all remote operations on a component are finished.
	 *  The effective logout is done when all remote operations on all components have been notified as finished.
	 */
	public void tryLogout() {
		if (logoutState.stillWaiting())
			return;
		
		// TODO: check session expiration
		remotingChannel.logout(new ResultFaultIssuesResponseListener() {
			@Override
			public void onResult(final ResultEvent event) {
				context.callLater(new Runnable() {
					public void run() {
						log.info("Application session logged out");
						
						handleResult(context, null, "logout", null, null, null);
						context.getContextManager().destroyContexts(false);
						
						logoutState.loggedOut(new TideResultEvent<Object>(context, null, event.getResult()));
					}
				});
			}

			@Override
			public void onFault(final FaultEvent event) {
				context.callLater(new Runnable() {
					public void run() {
						log.error("Could not log out %s", event.getDescription());
						
						handleFault(context, null, "logout", event.getMessage());
						
				        Fault fault = new Fault(event.getCode(), event.getDescription(), event.getDetails());
				        fault.setContent(event.getMessage());
				        fault.setCause(event.getCause());				        
						logoutState.loggedOut(new TideFaultEvent(context, null, fault, event.getExtended()));
					}
				});
			}
			
			@Override
			public void onIssue(final IssueEvent event) {
				context.callLater(new Runnable() {
					public void run() {
						log.error("Could not logout %s", event.getType());
						
						handleFault(context, null, "logout", null);
						
				        Fault fault = new Fault(Code.SERVER_CALL_FAILED, event.getType().name(), "");
						logoutState.loggedOut(new TideFaultEvent(context, null, fault, null));
					}
				});
			}
		});
	}
	
	
    /**
     *  @private  
     *  (Almost) abstract method: manages a remote call result
     *  This should be called by the implementors at the end of the result processing
     * 
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param ires invocation result object
     *  @param result result object
     *  @param mergeWith previous value with which the result will be merged
     */
    public void handleResult(Context context, String componentName, String operation, InvocationResult invocationResult, Object result, Object mergeWith) {
        trackingContext.clearPendingUpdates();
        
        log.debug("result {0}", result);
        
        List<ContextUpdate> resultMap = null;
        List<Update> updates = null;
        
        EntityManager entityManager = context.getEntityManager();
        BeanManager beanManager = context.getBeanManager();
        
        try {
            trackingContext.setEnabled(false);
            
            // Clear flash context variable for Grails/Spring MVC
            context.remove("flash");
            
            MergeContext mergeContext = entityManager.initMerge();
            mergeContext.setServerSession(this);
            
            boolean mergeExternal = true;
            if (invocationResult != null) {
                mergeExternal = invocationResult.getMerge();
                
                if (invocationResult.getUpdates() != null && invocationResult.getUpdates().length > 0) {
                    updates = new ArrayList<Update>(invocationResult.getUpdates().length);
                    for (Object[] u : invocationResult.getUpdates())
                        updates.add(Update.forUpdate((String)u[0], u[1]));
                    entityManager.handleUpdates(mergeContext, null, updates);
                }
                
                // Handle scope changes
                // TODO: merge context variables
    //            if (_componentStore.isComponentInEvent(componentName) && invocationResult.scope == Tide.SCOPE_SESSION && !meta_isGlobal) {
    //                var instance:Object = this[componentName];
    //                this[componentName] = null;
    //                _componentStore.getDescriptor(componentName).scope = Tide.SCOPE_SESSION;
    //                this[componentName] = instance;
    //            }
                
    //            componentRegistry.setScope(componentName, ScopeType.values()[invocationResult.getScope()]);
    //            componentRegistry.setRestrict(componentName, invocationResult.getRestrict() ? RestrictMode.YES : RestrictMode.NO);
                
                resultMap = invocationResult.getResults();
                
                if (resultMap != null) {
                    log.debug("result conversationId {0}", context.getContextId());
                    
                    // Order the results by container, i.e. 'person.contacts' has to be evaluated after 'person'
                    Collections.sort(resultMap, RESULTS_COMPARATOR);
                    
                    for (int k = 0; k < resultMap.size(); k++) {
                        ContextUpdate r = resultMap.get(k);
                        Object val = r.getValue();
                        
                        log.debug("update expression {0}: {1}", r, val);
    
                        String compName = r.getComponentName();
                        // TODO: merge context variables
    //                    if (compName == null && val != null) {
    //                        var t:Type = Type.forInstance(val);
    //                        compName = ComponentStore.internalNameForTypedComponent(t.name + '_' + t.id);
    //                    }
                        
                        trackingContext.addLastResult(compName 
                                + (r.getComponentClassName() != null ? "(" + r.getComponentClassName() + ")" : "") 
                                + (r.getExpression() != null ? "." + r.getExpression() : ""));
                        
                        // TODO: merge context variables
    //                    if (val != null) {
    //                        if (_componentStore.getDescriptor(compName).restrict == Tide.RESTRICT_UNKNOWN)
    //                            _componentStore.getDescriptor(compName).restrict = r.restrict ? Tide.RESTRICT_YES : Tide.RESTRICT_NO;
    //                        
    //                        if (_componentStore.getDescriptor(compName).scope == Tide.SCOPE_UNKNOWN)
    //                            _componentStore.getDescriptor(compName).scope = r.scope;
    //                    }
    //                    _componentStore.setComponentGlobal(compName, true);
                        
                        Object obj = context.byNameNoProxy(compName);
                        String[] p = r.getExpression() != null ? r.getExpression().split("\\.") : null;
                        if (p != null && p.length > 1) {
                            for (int i = 0; i < p.length-1; i++)
                                obj = beanManager.getProperty(obj, p[i]);
                        }
    //                    else if (p.length == 0)
    //                        _componentStore.setComponentRemoteSync(compName, Tide.SYNC_BIDIRECTIONAL);
                        
                        Object previous = null;
                        String propName = null;
                        if (p != null && p.length > 0) {
                            propName = p[p.length-1];
                            
                            if (obj instanceof PropertyHolder)
                                previous = beanManager.getProperty(((PropertyHolder)obj).getObject(), propName);
                            else if (obj != null)
                                previous = beanManager.getProperty(obj, propName);
                        }
                        else
                            previous = obj;
                        
                        // Don't merge with temporary properties
                        // TODO: merge context variables
                        if (previous instanceof Component) //  || previous instanceof ComponentProperty)
                            previous = null;
                        
                        Expression res = new ContextResult(r.getComponentName(), r.getExpression());
                        // TODO: merge context variables
    //                    var res:IExpression = ComponentStore.isInternalNameForTypedComponent(compName)
    //                        ? new TypedContextExpression(compName, r.expression)
    //                        : new ContextResult(r.componentName, r.expression);
                        
    //                    if (!isGlobal() && r.getScope() == ScopeType.SESSION.ordinal())
    //                        val = parentContext.getEntityManager().mergeExternal(val, previous, res);
    //                    else
                        val = entityManager.mergeExternal(mergeContext, val, previous, res, null, null, null, false);
                        
                        if (propName != null) {
                            if (obj instanceof PropertyHolder) {
                                ((PropertyHolder)obj).setProperty(propName, val);
                            }
                            else if (obj != null)
                                beanManager.setProperty(obj, propName, val);
                        }
                        else
                            context.set(compName, val);
                    }
                }
            }
            
            // Merges final result object
            if (result != null) {
                if (mergeExternal)
                    result = entityManager.mergeExternal(mergeContext, result, mergeWith, null, null, null, null, false);
                else
                    log.debug("skipped merge of remote result");
                if (invocationResult != null)
                    invocationResult.setResult(result);
            }
        }
        finally {
            MergeContext.destroy(entityManager);
            
            trackingContext.setEnabled(true);
        }

        // TODO: Seam 2 status messages support
//        if (componentRegistry.isComponent("statusMessages"))
//            get("statusMessages").setFromServer(invocationResult);
        
        // Dispatch received data update events         
        if (invocationResult != null) {
            trackingContext.removeResults(resultMap);
            
            // Dispatch received data update events
            if (updates != null)
                entityManager.raiseUpdateEvents(context, updates);
            
            // TODO: dispatch received context events
//            List<ContextEvent> events = invocationResult.getEvents();
//            if (events != null && events.size() > 0) {
//                for (ContextEvent event : events) {
//                    if (event.params[0] is Event)
//                        meta_dispatchEvent(event.params[0] as Event);
//                    else if (event.isTyped())
//                        meta_internalRaiseEvent("$TideEvent$" + event.eventType, event.params);
//                    else
//                        _tide.invokeObservers(this, TideModuleContext.currentModulePrefix, event.eventType, event.params);
//                }
//            }
        }
        
        log.debug("result merged into local context");
    }

    /**
     *  @private 
     *  Abstract method: manages a remote call fault
     * 
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param emsg error message
     */
    public void handleFault(Context context, String componentName, String operation, FaultMessage emsg) {
        trackingContext.clearPendingUpdates();

        // TODO: Seam 2 status messages support
//        if (emsg != null && emsg.getExtendedData() != null && componentRegistry.isComponent("statusMessages"))
//            get("statusMessages").setFromServer(emsg.getExtendedData());
    }
    
    private static final ResultsComparator RESULTS_COMPARATOR = new ResultsComparator();
	
	
	private static class LogoutState extends Observable {
		
		private boolean logoutInProgress = false;
		private int waitForLogout = 0;
		private boolean sessionExpired = false;
		private Timer logoutTimeout = null;
		
		public void logout(Observer logoutObserver, TimerTask forceLogout) {
			logout(logoutObserver);
			logoutTimeout = new Timer(true);
			logoutTimeout.schedule(forceLogout, 1000L);
		}
		
		public void logout(Observer logoutObserver) {
			addObserver(logoutObserver);
	        logoutInProgress = true;
		    waitForLogout = 1;
		}
		
		public void checkWait() {
			if (logoutInProgress)
				waitForLogout++;
		}
		
		public boolean stillWaiting() {
			if (sessionExpired)
				return false;
			
			if (!logoutInProgress)
				return true;
			
			waitForLogout--;
			if (waitForLogout > 0)
				return true;
			
			return false;
		}
		
		@SuppressWarnings("unused")
		public boolean isSessionExpired() {
			return sessionExpired;
		}
		
		public void loggedOut(TideRpcEvent event) {
			if (logoutTimeout != null) {
				logoutTimeout.cancel();
				logoutTimeout = null;
			}
			
			setChanged();
			notifyObservers(event);
			deleteObservers();
			
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = false;
		}
		
		public void sessionExpired() {
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = true;
		}
	}
	
	
	public interface Status {

		public boolean isBusy();
		
		public void setBusy(boolean busy);
		
		public boolean isConnected();
		
		public void setConnected(boolean connected);
		
		public boolean isShowBusyCursor();
		
		public void setShowBusyCursor(boolean showBusyCursor);
	}

	
	public static class DefaultStatus implements Status {
		
		private boolean showBusyCursor = true;
		
		private boolean connected = false;
		private boolean busy = false;

		@Override
		public boolean isBusy() {
			return busy;
		}
		
		public void setBusy(boolean busy) {
			this.busy = busy;
		}

		@Override
		public boolean isConnected() {
			return connected;
		}

		public void setConnected(boolean connected) {
			this.connected = connected;
		}

		@Override
		public boolean isShowBusyCursor() {
			return showBusyCursor;
		}

		@Override
		public void setShowBusyCursor(boolean showBusyCursor) {
			this.showBusyCursor = showBusyCursor;			
		}		
	}
    
	
    /**
     *  @private
     *  Comparator for expression evaluation ordering
     * 
     *  @param r1 expression 1
     *  @param r2 expression 2
     *  @param fields unused
     * 
     *  @return comparison value
     */
    private static class ResultsComparator implements Comparator<ContextUpdate> {
        
        public int compare(ContextUpdate r1, ContextUpdate r2) {
            
            if (r1.getComponentClassName() != null && r2.getComponentClassName() != null && !r1.getComponentClassName().equals(r2.getComponentClassName()))
                return r1.getComponentClassName().compareTo(r2.getComponentClassName());
            
            if (r1.getComponentName() != r2.getComponentName())
                return r1.getComponentName().compareTo(r2.getComponentName());
            
            if (r1.getExpression() == null)
                return r2.getExpression() == null ? 0 : -1;
            
            if (r2.getExpression() == null)
                return 1;
            
            if (r1.getExpression().equals(r2.getExpression()))
                return 0;
            
            if (r1.getExpression().indexOf(r2.getExpression()) == 0)
                return 1;
            if (r2.getExpression().indexOf(r1.getExpression()) == 0)
                return -1;
            
            return r1.getExpression().compareTo(r2.getExpression()) < 0 ? -1 : 0;
        }
    }

}
