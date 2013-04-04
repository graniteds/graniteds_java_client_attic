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

import java.util.concurrent.ExecutionException;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.FaultException;
import org.granite.client.tide.server.TideResponder;

/**
 * @author William DRAI
 */
public class ComponentListenerImpl<T> implements ComponentListener<T> {
    
    private Context sourceContext;
    private Component component;
    private String componentName;
    private String operation;
    private Object[] args;
    private Handler<T> handler;
    private TideResponder<T> tideResponder;
    private Object info;
    private boolean waiting = false;
    private Runnable responseHandler = null;
    private T result = null;
    private Exception exception = null;
    
    
    public ComponentListenerImpl(Context sourceContext, Handler<T>handler, Component component, String operation, Object[] args, Object info, TideResponder<T> tideResponder) {        
        this.sourceContext = sourceContext;
        this.handler = handler;
        this.component = component;
        this.componentName = component != null ? component.getName() : null;
        this.operation = operation;
        this.args = args;
        this.tideResponder = tideResponder;
        this.info = info;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public Object[] getArgs() {
        return args;
    }
    public void setArgs(Object[] args) {
        this.args = args;
    }
    
    public Context getSourceContext() {
        return sourceContext;
    }
    
    public Component getComponent() {
        return component;
    }
    
    public T getResult() throws InterruptedException, ExecutionException {
        synchronized (this) {
        	if (responseHandler == null && exception == null) {
	        	waiting = true;
	        	wait();
        	}
        	if (responseHandler != null)
        		responseHandler.run();
        }
        if (exception instanceof ExecutionException)
        	throw (ExecutionException)exception;
        else if (exception instanceof InterruptedException)
        	throw (InterruptedException)exception;
    	return result;
    }
    
    public void setResult(T result) {
    	this.result = result;
    }
    
	@Override
    public void onResult(ResultEvent event) {
		Runnable h = handler.result(sourceContext, event, info, componentName, operation, tideResponder, this);
        synchronized (this) {
        	System.out.println("onResult, waiting " + waiting + ", handler: " + h);
    		responseHandler = h;
        	if (waiting)
        		notifyAll();
        	else
        		sourceContext.callLater(h);
        }
    }
    
	@Override
    public void onFault(FaultEvent event) {
		Runnable h = handler.fault(sourceContext, event, info, componentName, operation, tideResponder, this);
		synchronized (this) {
			responseHandler = h;
			exception = new FaultException(event);
			if (waiting)
				notifyAll();
			else
        		sourceContext.callLater(h);
		}
    }

	@Override
	public void onFailure(final FailureEvent event) {
		synchronized (this) {
			exception = new ExecutionException(event.getCause());
			if (waiting)
				notifyAll();
		}
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		synchronized (this) {
			exception = new InterruptedException("timeout");
			if (waiting)
				notifyAll();
		}
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		synchronized (this) {
			exception = new InterruptedException("cancel");
			if (waiting)
				notifyAll();
		}
	}
}
