package org.granite.tide.spring;

import org.granite.tide.ContextAware;
import org.granite.tide.NameAware;
import org.granite.tide.Platform;
import org.granite.tide.PlatformConfigurable;
import org.granite.tide.impl.ContextManagerImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringContextManager extends ContextManagerImpl implements ApplicationContextAware, BeanPostProcessor, BeanFactoryPostProcessor {
	
	public SpringContextManager(Platform platform) {
		super(platform);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		setInstanceStoreFactory(new SpringInstanceStoreFactory(applicationContext));
	}
		
	@Override
	public Object postProcessBeforeInitialization(Object instance, String name) throws BeansException {
    	if (name != null && instance instanceof NameAware)
    		((NameAware)instance).setName(name);
    	if (instance instanceof ContextAware)
    		((ContextAware)instance).setContext(getContext(null));
    	if (instance.getClass().isAnnotationPresent(PlatformConfigurable.class))
    		platform.configure(instance);
    	return instance;
	}
	
	@Override
	public Object postProcessAfterInitialization(Object instance, String name) throws BeansException {
		return instance;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		beanFactory.registerSingleton("context", getContext(null));
		beanFactory.registerSingleton("entityManager", getContext(null).getEntityManager());
		beanFactory.registerSingleton("dataManager", getContext(null).getDataManager());
	}
}
