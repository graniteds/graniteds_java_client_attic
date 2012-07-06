package org.granite.client.tide.impl;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentResponder;
import org.granite.client.tide.server.TideResponder;


/**
 * @author William DRAI
 */
public class ComponentResponderImpl implements ComponentResponder {
    
    private Context sourceContext;
    private Component component;
    private String componentName;
    private String operation;
    private Object[] args;
    private Handler handler;
    private TideResponder<?> tideResponder;
    private Object info;
    
    
    public ComponentResponderImpl(Context sourceContext, Handler handler, Component component, String operation, Object[] args, Object info, TideResponder<?> tideResponder) {        
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
		// TODO Auto-generated method stub		
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		// TODO Auto-generated method stub
	}    
}
