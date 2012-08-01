package org.granite.client.tide.spring;

import org.granite.client.tide.ContextAware;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.Platform;
import org.granite.client.tide.PlatformConfigurable;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringContextManager extends SimpleContextManager implements ApplicationContextAware, BeanPostProcessor, BeanFactoryPostProcessor {
	
	private ApplicationContext applicationContext;
	
	public SpringContextManager(Platform platform) {
		super(platform);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
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
		EntityManager entityManager = getContext(null).getEntityManager();
		entityManager.addListener(new SpringDataConflictListener());
		beanFactory.registerSingleton("entityManager", entityManager);
		beanFactory.registerSingleton("dataManager", getContext(null).getDataManager());
	}
	
	private final class SpringDataConflictListener implements DataConflictListener {
		@Override
		public void onConflict(EntityManager entityManager, Conflicts conflicts) {
			TideApplicationEvent event = new TideApplicationEvent(getContext(null), "org.granite.client.tide.data.conflict", conflicts);
			applicationContext.publishEvent(event);
		}
	}
}
