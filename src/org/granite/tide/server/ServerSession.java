package org.granite.tide.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.granite.logging.Logger;
import org.granite.messaging.Channel;
import org.granite.messaging.Consumer;
import org.granite.messaging.MessageAgent;
import org.granite.messaging.Producer;
import org.granite.messaging.WebSocketChannel;
import org.granite.messaging.engine.Engine;
import org.granite.messaging.engine.EngineException;
import org.granite.messaging.engine.EngineStatusHandler;
import org.granite.messaging.engine.HttpClientEngine;
import org.granite.messaging.engine.WebSocketEngine;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.MessageEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.rpc.remoting.RemoteObject;
import org.granite.tide.BeanManager;
import org.granite.tide.Context;
import org.granite.tide.ContextAware;
import org.granite.tide.Expression;
import org.granite.tide.PlatformConfigurable;
import org.granite.tide.PropertyHolder;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.EntityManager.Update;
import org.granite.tide.data.spi.MergeContext;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationResult;

import flex.messaging.messages.ErrorMessage;


@PlatformConfigurable
public class ServerSession implements ContextAware {

	private static Logger log = Logger.getLogger(ServerSession.class);
	
	public static final String SESSION_ID_TAG = "org.granite.sessionId";
    public static final String CONVERSATION_TAG = "conversationId";
    public static final String CONVERSATION_PROPAGATION_TAG = "conversationPropagation";
    public static final String IS_LONG_RUNNING_CONVERSATION_TAG = "isLongRunningConversation";
    public static final String WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG = "wasLongRunningConversationEnded";
    public static final String WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG = "wasLongRunningConversationCreated";
    public static final String IS_FIRST_CALL_TAG = "org.granite.tide.isFirstCall";
    public static final String IS_FIRST_CONVERSATION_CALL_TAG = "org.granite.tide.isFirstConversationCall";
	
	public static final String LOGIN = "org.granite.tide.login";
	public static final String LOGOUT = "org.granite.tide.logout";
	public static final String SESSION_EXPIRED = "org.granite.tide.sessionExpired";
    
    
	@SuppressWarnings("unused")
	private boolean confChanged = false;
    private HttpClientEngine httpClientEngine = null;
    private WebSocketEngine webSocketEngine = null;
    private String protocol = "http";
    private String contextRoot = "";
    private String serverName = "{server.name}";
    private String serverPort = "{server.port}";
    private String graniteUrlMapping = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
    private String gravityUrlMapping = "/gravityamf/amf.txt";
    
    private URI graniteURI;
	private URI gravityURI;
    
	private Context context = null;
    private TrackingContext trackingContext = new TrackingContext();
	
	private Status status = new DefaultStatus();
	
	private String sessionId = null;
	private boolean isFirstCall = true;
	
	private LogoutState logoutState = new LogoutState();
		
	private String destination = null;
    private Channel graniteChannel;
	private WebSocketChannel gravityChannel;
	protected Map<String, RemoteObject> remoteObjects = new HashMap<String, RemoteObject>();
	protected Map<String, MessageAgent> messageAgents = new HashMap<String, MessageAgent>();
	
	
    public ServerSession() throws Exception {    	
    }
    
    public ServerSession(String destination, String contextRoot, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
        this(destination, contextRoot, "{server.name}", "{server.port}", graniteUrlMapping, gravityUrlMapping);
    }

    public ServerSession(String destination, String contextRoot, String serverName, String serverPort, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
    	this(destination, "http", contextRoot, serverName, serverPort, graniteUrlMapping, gravityUrlMapping);
    }
    
    public ServerSession(String destination, String protocol, String contextRoot, String serverName, String serverPort, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
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

    
    public void setHttpClientEngine(HttpClientEngine engine) {
		this.httpClientEngine = engine;
    	confChanged = true;
	}

    public void setWebSocketEngine(WebSocketEngine engine) {
		this.webSocketEngine = engine;
    	confChanged = true;
	}

    public Engine getHttpClientEngine() {
    	return httpClientEngine;
    }
    
    public WebSocketEngine getWebSocketEngine() {
    	return webSocketEngine;
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
    
    public void setServerPort(String serverPort) {
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
	
	public void start() throws Exception {
		if (httpClientEngine == null)
			log.warn("No http client engine defined for server session, remoting disabled");
		else {
			httpClientEngine.setStatusHandler(new ServerSessionStatusHandler());
			httpClientEngine.start();
			graniteURI = new URI(protocol + "://" + this.serverName + ":" + this.serverPort + this.contextRoot + this.graniteUrlMapping);
			graniteChannel = new Channel(httpClientEngine, "graniteamf", graniteURI);
		}		
		
		if (webSocketEngine == null)
			log.warn("No websocket engine defined for server session, messaging disabled");
		else {
			webSocketEngine.setStatusHandler(new ServerSessionStatusHandler());
			webSocketEngine.start();
			gravityURI = new URI(protocol.replace("http", "ws") + "://" + this.serverName + ":" + this.serverPort + this.contextRoot + this.gravityUrlMapping);
			gravityChannel = new WebSocketChannel(webSocketEngine, "gravityamf", gravityURI);
		}
	}
	
	public void stop()throws Exception {
		if (httpClientEngine != null) {
			graniteChannel = null;
			httpClientEngine.stop();
		}
		if (webSocketEngine != null) {
			gravityChannel = null;
			webSocketEngine.stop();
		}
	}
	
	public RemoteObject getRemoteObject() {
		return getRemoteObject(destination);
	}
	public synchronized RemoteObject getRemoteObject(String destination) {
		if (graniteChannel == null)
			throw new IllegalStateException("Engine not started for server session");
		
		RemoteObject remoteObject = remoteObjects.get(destination);
		if (remoteObject == null) {
			remoteObject = new RemoteObject(destination);
			remoteObject.setChannel(graniteChannel);
			remoteObjects.put(destination, remoteObject);
		}
		return remoteObject;
	}

	public synchronized Consumer getConsumer(String destination) {
		if (gravityChannel == null)
			throw new IllegalStateException("Engine not started for server session");
		
		MessageAgent consumer = messageAgents.get(destination);
		if (consumer == null) {
			consumer = new Consumer(destination);
			consumer.setChannel(gravityChannel);
			messageAgents.put(destination, consumer);
		}
		return consumer instanceof Consumer ? (Consumer)consumer : null;
	}

	public synchronized Producer getProducer(String destination) {
		if (gravityChannel == null)
			throw new IllegalStateException("Engine not started for server session");
		
		MessageAgent producer = messageAgents.get(destination);
		if (producer == null) {
			producer = new Producer(destination);
			producer.setChannel(gravityChannel);
			messageAgents.put(destination, producer);
		}
		return producer instanceof Producer ? (Producer)producer : null;
	}
	
	public boolean isFirstCall() {
		return isFirstCall;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void trackCall() {
		isFirstCall = false;
	}
	
	public void handleResultEvent(MessageEvent event) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		if (gravityChannel != null && sessionId != null)
			gravityChannel.setSessionId(sessionId);
		isFirstCall = false;
		status.setConnected(true);
	}
	
	public void handleFaultEvent(FaultEvent event, ErrorMessage emsg) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		if (gravityChannel != null && sessionId != null)
			gravityChannel.setSessionId(sessionId);
		
        if (emsg != null && emsg.getFaultCode().equals("Connection.Failed"))
        	status.setConnected(false);            
	}
	
	public class ServerSessionStatusHandler implements EngineStatusHandler {

		@Override
		public void handleIO(boolean active) {
			status.setBusy(active);
		}

		@Override
		public void handleException(EngineException e) {
			log.error(e, "Engine failed");
		}		
	}
	
	
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
    	getRemoteObject().setCredentials(username, password);
    }
    
    public void afterLogin() {
		log.info("Application login");
		
		context.getEventBus().raiseEvent(context, LOGIN);
    }
    
    public void sessionExpired() {
		log.info("Application session expired");
		
		sessionId = null;
		isFirstCall = true;
		
		logoutState.sessionExpired(new TimerTask() {
			@Override
			public void run() {
				logoutState.sessionExpired();
				tryLogout();
			}
		});
		
		context.getEventBus().raiseEvent(context, SESSION_EXPIRED);
		
        tryLogout();
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
		
		graniteChannel.logout(new AsyncResponder() {
			@Override
			public void result(final ResultEvent event) {
				context.callLater(new Runnable() {
					public void run() {
						log.info("Application logout");
						
						handleResult(context, null, "logout", null, null, null);
						context.getContextManager().destroyContexts(false);
												
						logoutState.loggedOut(new TideResultEvent<Object>(context, event.getToken(), null, event.getResult()));
					}
				});
			}

			@Override
			public void fault(final FaultEvent event) {
				context.callLater(new Runnable() {
					public void run() {
						log.error("Could not logout %s", event.getFaultString());
						
						handleFault(context, null, "logout", event.getMessage());
						
				        Fault fault = new Fault(event.getFaultCode(), event.getFaultString(), event.getFaultDetail());
				        fault.setContent(event.getMessage());
				        fault.setRootCause(event.getRootCause());				        
						logoutState.loggedOut(new TideFaultEvent(context, event.getToken(), null, fault, event.getExtendedData()));
					}
				});
			}
		}, logoutState.isSessionExpired());
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
                // TODO: migrate
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
                        // TODO
    //                    if (compName == null && val != null) {
    //                        var t:Type = Type.forInstance(val);
    //                        compName = ComponentStore.internalNameForTypedComponent(t.name + '_' + t.id);
    //                    }
                        
                        trackingContext.addLastResult(compName 
                                + (r.getComponentClassName() != null ? "(" + r.getComponentClassName() + ")" : "") 
                                + (r.getExpression() != null ? "." + r.getExpression() : ""));
                        
                        // TODO
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
                        // TODO
                        if (previous instanceof Component) //  || previous instanceof ComponentProperty)
                            previous = null;
                        
                        Expression res = new ContextResult(r.getComponentName(), r.getExpression());
                        // TODO
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

        // TODO
//        if (componentRegistry.isComponent("statusMessages"))
//            get("statusMessages").setFromServer(invocationResult);
        
        // Dispatch received data update events         
        if (invocationResult != null) {
            trackingContext.removeResults(resultMap);
            
            // Dispatch received data update events
            if (updates != null)
                entityManager.raiseUpdateEvents(context, updates);
            
            // Dispatch received context events
            // TODO
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
    public void handleFault(Context context, String componentName, String operation, ErrorMessage emsg) {
        trackingContext.clearPendingUpdates();

        // TODO
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
		
		public void sessionExpired(TimerTask forceLogout) {
			sessionExpired();
			logoutTimeout = new Timer(true);
			logoutTimeout.schedule(forceLogout, 1000L);
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
