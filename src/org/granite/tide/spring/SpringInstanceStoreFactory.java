package org.granite.tide.spring;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.granite.tide.Context;
import org.granite.tide.InstanceStore;
import org.granite.tide.InstanceStoreFactory;
import org.springframework.context.ApplicationContext;


public class SpringInstanceStoreFactory implements InstanceStoreFactory {
	
	private final ApplicationContext applicationContext;
	
	public SpringInstanceStoreFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public InstanceStore createStore(Context context) {
		return new SpringInstanceStore(context, applicationContext);
	}

	
	public static class SpringInstanceStore implements InstanceStore {
		
		@SuppressWarnings("unused")
		private final Context context;
		private final ApplicationContext applicationContext;
		
		public SpringInstanceStore(Context context, ApplicationContext applicationContext) {
			this.context = context;
			this.applicationContext = applicationContext;
		}
	    
		@Override
		public <T> T getNoProxy(String name) {
			return null;
		}

		@Override
		public void set(String name, Object instance) {
		}

		@Override
		public void set(Object instance) {
		}

		@Override
		public void remove(String name) {
		}

		@Override
		public List<String> allNames() {
			return Arrays.asList(applicationContext.getBeanDefinitionNames());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T byName(String name, Context context) {
			return (T)applicationContext.getBean(name);
		}

		@Override
		public <T> T byType(Class<T> type, Context context) {
			return applicationContext.getBean(type);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] allByType(Class<T> type, Context context) {
			Map<String, T> instancesMap = applicationContext.getBeansOfType(type);
			T[] all = (T[])Array.newInstance(type, instancesMap.size());
			return instancesMap.values().toArray(all);
		}
	}
}
