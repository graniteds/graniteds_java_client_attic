/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.impl;

import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.tide.invocation.InvocationResult;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideMergeResponder;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;

/**
 * @author William DRAI
 */
public class ResultHandler<T> implements Runnable {

	private final ServerSession serverSession;
	private final Context sourceContext;
	private final String componentName;
	private final String operation;
	private final Event event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener componentResponder;
	
	
	public ResultHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation, Event event, Object info, TideResponder<T> tideResponder, ComponentListener componentResponder) {
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
        InvocationResult invocationResult = null;
        Object result = null; 
        if (event instanceof ResultEvent)
            result = ((ResultEvent)event).getResult();
        else if (event instanceof IncomingMessageEvent<?>)
            result = ((IncomingMessageEvent<?>)event).getMessage();
        
        if (result instanceof InvocationResult) {
            invocationResult = (InvocationResult)result;
            result = invocationResult.getResult();
        }
        
//        var conversationId:String = null;
//        if (event.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG])
//            conversationId = event.message.headers[Tide.CONVERSATION_TAG];
//        var wasConversationCreated:Boolean = event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
//        var wasConversationEnded:Boolean = event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
//        
//        var context:Context = _contextManager.retrieveContext(sourceContext, conversationId, wasConversationCreated, wasConversationEnded);	        
        
        Context context = sourceContext.getContextManager().retrieveContext(sourceContext, null, false, false); // conversationId, wasConversationCreated, wasConversationEnded);
        
        serverSession.handleResultEvent(event);
        
        serverSession.handleResult(context, componentName, operation, invocationResult, result, 
            tideResponder instanceof TideMergeResponder<?> ? ((TideMergeResponder<T>)tideResponder).getMergeResultWith() : null);
        if (invocationResult != null)
            result = invocationResult.getResult();
        
        @SuppressWarnings("unused")
		boolean handled = false;
        if (tideResponder != null) {
            @SuppressWarnings("unchecked")
            TideResultEvent<T> resultEvent = new TideResultEvent<T>(context, componentResponder, (T)result);
            tideResponder.result(resultEvent);
            if (resultEvent.isDefaultPrevented())
                handled = true;
        }
        
//	        context.clearData();
//	        
//	        // Should be after event result handling and responder: previous could trigger other remote calls
//	        if (context.isFinished())
//	            context.scheduleDestroy();
//	        
//	        if (!handled && !serverSession.isLogoutInProgress())
//	            context.raiseEvent(Tide.CONTEXT_RESULT, result);
        
        serverSession.tryLogout();
    }
}