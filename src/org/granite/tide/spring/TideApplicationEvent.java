package org.granite.tide.spring;

import org.granite.tide.Context;
import org.granite.tide.events.TideEvent;
import org.springframework.context.ApplicationEvent;


public class TideApplicationEvent extends ApplicationEvent implements TideEvent {

	private static final long serialVersionUID = 1L;
	
	private final String type;
	private final Object[] args;

	
	public TideApplicationEvent(Object source, String type, Object... args) {
		super(source);
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
