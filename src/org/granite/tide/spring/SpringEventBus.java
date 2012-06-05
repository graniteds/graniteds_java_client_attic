package org.granite.tide.spring;

import org.granite.tide.Context;
import org.granite.tide.impl.SimpleEventBus;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;


public class SpringEventBus extends SimpleEventBus implements ApplicationContextAware, ApplicationListener<TideApplicationEvent> {

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void raiseEvent(Context context, String type, Object... args) {
		applicationContext.publishEvent(new TideApplicationEvent(context, type, args));
	}

	@Override
	public void onApplicationEvent(TideApplicationEvent event) {
	    raiseEvent(event);
	}

}
