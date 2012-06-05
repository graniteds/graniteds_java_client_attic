package org.granite.tide.rpc;

import java.net.URI;
import java.util.HashMap;
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
import org.granite.rpc.AsyncToken;
import org.granite.rpc.InvocationInterceptor;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.MessageEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.rpc.remoting.RemoteObject;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.ContextAware;
import org.granite.tide.PlatformConfigurable;
import org.granite.tide.TideResponder;

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
    
	private String destination = null;
	private Context context = null;
	
	private Status status = new DefaultStatus();
	
	private String sessionId = null;
	private boolean isFirstCall = true;
	
	private LogoutState logoutState = new LogoutState();
	
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

	public Consumer getConsumer(String destination) {
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

	public Producer getProducer(String destination) {
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
	
	public AsyncToken remoteCall(String method, Object[] params, AsyncResponder responder) {
        RemoteObject ro = getRemoteObject();
        if (ro == null)
        	throw new RuntimeException("Cannot call remote server, internal RemoteObject not created");
        
        AsyncToken token = ro.call(method, params, responder);
        
        checkWaitForLogout();
        
        return token;
	}
	
	public void call() {
		isFirstCall = false;
	}
	
	public void result(MessageEvent event) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		if (gravityChannel != null && sessionId != null)
			gravityChannel.setSessionId(sessionId);
		isFirstCall = false;
		status.setConnected(true);
	}
	
	public void fault(FaultEvent event, ErrorMessage emsg) {
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AsyncToken invoke(Context context, Component component, String operation, Object[] args, TideResponder<?> tideResponder, 
                           boolean withContext, ComponentResponder.Handler handler) {
        log.debug("invokeComponent %s > %s.%s", context.getContextId(), component.getName(), operation);
        
        ComponentResponder.Handler h = handler != null ? handler : new ComponentResponder.Handler() {            
			@Override
            public void result(Context context, ResultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder) {
            	context.callLater(new ResultHandler(ServerSession.this, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
            
            @Override
            public void fault(Context context, FaultEvent event, Object info, String componentName,
                    String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder) {
            	context.callLater(new FaultHandler(ServerSession.this, context, componentName, operation, event, info, tideResponder, componentResponder));
            }
        };
        ComponentResponder componentResponder = new ComponentResponder(context, h, component, operation, args, null, tideResponder);
        
        InvocationInterceptor[] interceptors = context.allByType(InvocationInterceptor.class);
        if (interceptors != null) {
            for (InvocationInterceptor interceptor : interceptors)
                interceptor.beforeInvocation(context, component, operation, args, componentResponder);
        }
        
        context.getContextManager().destroyFinishedContexts();
        
//        // Force generation of uids by merging all arguments in the current context
//        for (int i = 0; i < args.length; i++) {
//            if (args[i] instanceof PropertyHolder)
//                args[i] = ((PropertyHolder)args[i]).getObject();
//            args[i] = entityManager.mergeExternal(args[i], null);
//        }
//        
//        // Call argument preprocessors before sending arguments to server
//        var method:Method = Type.forInstance(component).getInstanceMethodNoCache(op);
//        for each (var app:IArgumentPreprocessor in allByType(IArgumentPreprocessor, true))
//            componentResponder.args = app.preprocess(method, args);
        
        return component.invoke(componentResponder);
    }
	
    
    public void login(String username, String password) {
    	getRemoteObject().setCredentials(username, password);
    }
    
    public void afterLogin() {
		log.info("Application login");
		
		context.getEventBus().raiseEvent(context, LOGIN);
    }
    
    public void sessionExpired() {
		log.info("Application session expired");
		
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
						
						context.internalResult(null, null, "logout", null, null, null);
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
						
						context.internalFault(null, "logout", event.getMessage());
						
				        Fault fault = new Fault(event.getFaultCode(), event.getFaultString(), event.getFaultDetail());
				        fault.setContent(event.getMessage());
				        fault.setRootCause(event.getRootCause());				        
						logoutState.loggedOut(new TideFaultEvent(context, event.getToken(), null, fault, event.getExtendedData()));
					}
				});
			}
		});
	}
	
	
	private static class LogoutState extends Observable {
		
		private boolean logoutInProgress = false;
		private int waitForLogout = 0;
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
			if (!logoutInProgress)
				return true;
			
			waitForLogout--;
			if (waitForLogout > 0)
				return true;
			
			return false;
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
		}
		
		public void sessionExpired(TimerTask forceLogout) {
			sessionExpired();
			logoutTimeout = new Timer(true);
			logoutTimeout.schedule(forceLogout, 1000L);
		}
		
		public void sessionExpired() {
			logoutInProgress = false;
			waitForLogout = 0;
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
}
