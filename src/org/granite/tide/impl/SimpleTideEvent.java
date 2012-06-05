package org.granite.tide.impl;

import java.util.EventObject;

import org.granite.tide.Context;
import org.granite.tide.events.TideEvent;


public class SimpleTideEvent extends EventObject implements TideEvent {

	private static final long serialVersionUID = 1L;

	private final String type;
	private final Object[] args;
	
	
	public SimpleTideEvent(Context context, String type, Object... args) {
		super(context);
		this.type = type;
		this.args = args;
	}
	
	public Context getContext() {
		return (Context)getSource();
	}
	
	public String getType() {
		return type;
	}
	
	public Object[] getArgs() {
		return args;
	}
}
