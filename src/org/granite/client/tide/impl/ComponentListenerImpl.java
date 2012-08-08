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

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.TideResponder;

/**
 * @author William DRAI
 */
public class ComponentListenerImpl implements ComponentListener {
    
    private Context sourceContext;
    private Component component;
    private String componentName;
    private String operation;
    private Object[] args;
    private Handler handler;
    private TideResponder<?> tideResponder;
    private Object info;
    
    
    public ComponentListenerImpl(Context sourceContext, Handler handler, Component component, String operation, Object[] args, Object info, TideResponder<?> tideResponder) {        
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
    
	@Override
    public void onResult(ResultEvent event) {
        handler.result(sourceContext, event, info, componentName, operation, tideResponder, this);
        
    }
    
	@Override
    public void onFault(FaultEvent event) {
        handler.fault(sourceContext, event, info, componentName, operation, tideResponder, this);
    }

	@Override
	public void onFailure(FailureEvent event) {
		// TODO: handle listener failure 
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		// TODO: handle listener failure 
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		// TODO: handle listener failure 
	}
}
