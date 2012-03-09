package org.granite.tide.rpc;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.granite.logging.Logger;
import org.granite.messaging.Channel;
import org.granite.messaging.engine.ApacheAsyncEngine;
import org.granite.messaging.engine.Engine;
import org.granite.messaging.engine.EngineException;
import org.granite.messaging.engine.EngineStatusHandler;
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
    
    
	@SuppressWarnings("unused")
	private boolean confChanged = false;
    private Engine engine = new ApacheAsyncEngine();
    private String contextRoot = "";
    private String serverName = "{server.name}";
    private String serverPort = "{server.port}";
    private String graniteUrlMapping = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
    private String gravityUrlMapping = "/gravityamf/amf.txt";
    
    private URI graniteURI;
    @SuppressWarnings("unused")
	private URI gravityURI;
    
	private String destination = null;
	private Context context = null;
	
	private Status status = new DefaultStatus();
	
	private String sessionId = null;
	private boolean isFirstCall = true;
	
	private boolean logoutInProgress = false;
	private int waitForLogout = 0;
	
	// Remoting engine
    private Channel graniteChannel;
    @SuppressWarnings("unused")
	private Channel gravityChannel;
	protected Map<String, RemoteObject> remoteObjects = new HashMap<String, RemoteObject>();
	
	
    public ServerSession() throws Exception {    	
    }
    
    public ServerSession(String destination, String contextRoot, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
        this(destination, contextRoot, "{server.name}", "{server.port}", graniteUrlMapping, gravityUrlMapping);
    }

    public ServerSession(String destination, String contextRoot, String serverName, String serverPort, String graniteUrlMapping, String gravityUrlMapping) throws Exception {
        super();
        this.destination = destination;
        this.contextRoot = contextRoot;
        this.serverName = serverName;
        this.serverPort = serverPort;
        if (graniteUrlMapping != null)
        	this.graniteUrlMapping = graniteUrlMapping;
        if (gravityUrlMapping != null)
        	this.gravityUrlMapping = gravityUrlMapping;
    }

    
    public void setEngine(Engine engine) {
		this.engine = engine;
    	confChanged = true;
	}
    
    public Engine getEngine() {
    	return engine;
    }
	
    public void setContextRoot(String contextRoot) {
    	this.contextRoot = contextRoot;
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
		if (engine == null)
			throw new IllegalStateException("No engine define for server session");
		
		engine.setStatusHandler(new ServerSessionStatusHandler());
		engine.start();
		graniteURI = new URI("http" + "://" + this.serverName + ":" + this.serverPort + this.contextRoot + this.graniteUrlMapping);
		gravityURI = new URI("http" + "://" + this.serverName + ":" + this.serverPort + this.contextRoot + this.gravityUrlMapping);
		graniteChannel = new Channel(engine, "graniteamf", graniteURI);
	}
	
	public void stop()throws Exception {
		if (engine == null)
			throw new IllegalStateException("No engine define for server session");
		graniteChannel = null;
		engine.stop();
	}
	
	public RemoteObject getRemoteObject() {
		return getRemoteObject(destination);
	}
	public RemoteObject getRemoteObject(String destination) {
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
	
	public boolean isFirstCall() {
		return isFirstCall;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void call() {
		isFirstCall = false;
	}
	
	public void result(MessageEvent event) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		isFirstCall = false;
		status.setConnected(true);
	}
	
	public void fault(FaultEvent event, ErrorMessage emsg) {
		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
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
	
	
	public boolean isLogoutInProgress() {
		return logoutInProgress;
	}
	
	/**
	 * 	Notify the framework that it should wait for a async operation before effectively logging out.
	 *  Only if a logout has been requested.
	 */
	public void checkWaitForLogout() {
		isFirstCall = false;
		
		if (logoutInProgress)
			waitForLogout++;
	}
	
	/**
	 * 	Try logout. Should be called after all remote operations on a component are finished.
	 *  The effective logout is done when all remote operations on all components have been notified as finished.
	 */
	public void tryLogout() {
		if (!logoutInProgress)
			return;
		
		waitForLogout--;
		if (waitForLogout > 0)
			return;
		
		graniteChannel.logout(new AsyncResponder() {
			@Override
			public void result(ResultEvent event) {
				log.info("Application logout");
				
				context.getContextManager().destroyContexts(false);
				
				logoutInProgress = false;
				waitForLogout = 0;
				
				// context.raiseEvent("LOGGED_OUT");
			}

			@Override
			public void fault(FaultEvent event) {
				log.error("Could not logout %s", event.getFaultString());
			}
		});
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
