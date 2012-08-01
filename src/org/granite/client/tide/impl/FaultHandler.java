package org.granite.client.tide.impl;

import java.util.Map;

import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.Fault;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.logging.Logger;

/**
 *  @private
 *  Implementation of fault handler
 *  
 *  @param sourceContext source context of remote call
 *  @param info fault object
 *  @param componentName component name
 *  @param op remote operation
 *  @param tideResponder Tide responder for the remote call
 */
public class FaultHandler<T> implements Runnable {
	
	private static final Logger log = Logger.getLogger(FaultHandler.class);
	
	private final ServerSession serverSession;
	private final Context sourceContext;
	private final String componentName;
	private final String operation;
	private final FaultEvent event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener componentResponder;
	
	
	public FaultHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation, FaultEvent event, Object info, 
			TideResponder<T> tideResponder, ComponentListener componentResponder) {
		this.serverSession = serverSession;
		this.sourceContext = sourceContext;
		this.componentName = componentName;
		this.operation = operation;
		this.event = event;
		this.info = info;
		this.tideResponder = tideResponder;
		this.componentResponder = componentResponder;
	}

	public void run() {
        log.error("fault %s", event.toString());
       
        // TODO: conversation contexts
//        var sessionId:String = faultEvent.message.headers[Tide.SESSION_ID_TAG];
//        var conversationId:String = null;
//        if (faultEvent.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG])
//            conversationId = faultEvent.message.headers[Tide.CONVERSATION_TAG];
//        var wasConversationCreated:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
//        var wasConversationEnded:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
//        
//        var context:Context = _contextManager.retrieveContext(sourceContext, conversationId, wasConversationCreated, wasConversationEnded);
        
        Context context = sourceContext.getContextManager().retrieveContext(sourceContext, null, false, false);
        
       	FaultMessage emsg = event.getMessage();
       	FaultMessage m = emsg;
        Map<String, Object> extendedData = emsg != null ? emsg.getExtended() : null;
        do {
            if (m != null && m.getCode() != null && m.isSecurityFault()) {
                emsg = m;
                extendedData = emsg != null ? emsg.getExtended() : null;
                break;
            }
            // TODO: check WTF we should do here
            if (m != null && m.getCause() instanceof FaultEvent)
                m = (FaultMessage)((FaultEvent)m.getCause()).getCause();
            else if (m.getCause() instanceof FaultMessage)
                m = (FaultMessage)m.getCause();
            else
            	m = null;
        }
        while (m != null);
        
        serverSession.handleFaultEvent(event, emsg);
        
        serverSession.handleFault(context, componentName, operation, emsg);
        
        boolean handled = false;
        Fault fault = new Fault(emsg.getCode(), emsg.getDescription(), emsg.getDetails());
        fault.setContent(event.getMessage());
        fault.setCause(event.getCause());
        
        TideFaultEvent faultEvent = new TideFaultEvent(context, componentResponder, fault, extendedData);
        if (tideResponder != null) {
            tideResponder.fault(faultEvent);
            if (faultEvent.isDefaultPrevented())
                handled = true;
        }
        
        if (!handled) {
            ExceptionHandler[] exceptionHandlers = context.getContextManager().getContext(null).allByType(ExceptionHandler.class);
            if (exceptionHandlers != null && emsg != null) {
                // Lookup for a suitable exception handler
                for (ExceptionHandler handler : exceptionHandlers) {
                    if (handler.accepts(emsg)) {
                        handler.handle(context, emsg, faultEvent);
                        handled = true;
                        break;
                    }
                }
                if (!handled)
                    log.error("Unhandled fault: " + emsg.getCode() + ": " + emsg.getDescription());
            }
            else if (exceptionHandlers != null && exceptionHandlers.length > 0 && event.getMessage() instanceof FaultMessage) {
                // Handle fault with default exception handler
                exceptionHandlers[0].handle(context, (FaultMessage)event.getMessage(), faultEvent);
            }
            else {
                log.error("Unknown fault: " + event.toString());
            }
        }
        
        if (!handled && !serverSession.isLogoutInProgress())
        	context.getEventBus().raiseEvent(context, ServerSession.CONTEXT_FAULT, event.getMessage());
        
        serverSession.tryLogout();
    }
}