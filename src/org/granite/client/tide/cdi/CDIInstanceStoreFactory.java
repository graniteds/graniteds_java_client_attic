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

package org.granite.client.tide.cdi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.InstanceStoreFactory;

/**
 * @author William DRAI
 */
public class CDIInstanceStoreFactory implements InstanceStoreFactory {
	
	private final BeanManager beanManager;
	
	public CDIInstanceStoreFactory(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	@Override
	public InstanceStore createStore(Context context) {
		return new CDIInstanceStore(context, beanManager);
	}

	
	public static class CDIInstanceStore implements InstanceStore {
		
		@SuppressWarnings("unused")
		private final Context context;
		private final BeanManager beanManager;
		
		public CDIInstanceStore(Context context, BeanManager beanManager) {
			this.context = context;
			this.beanManager = beanManager;
		}
	    
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getNoProxy(String name) {
			Set<Bean<?>> beans = beanManager.getBeans(name);
			if (beans.size() == 0)
				throw new RuntimeException("Bean not found " + name);
			if (beans.size() > 1)
				throw new RuntimeException("Ambiguous beans found " + name);
			Bean<?> bean = beans.iterator().next();
			CreationalContext<?> cc = beanManager.createCreationalContext(bean);
			return (T)beanManager.getReference(bean, Object.class, cc);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T byName(String name, Context context) {
			Set<Bean<?>> beans = beanManager.getBeans(name);
			if (beans.size() == 0)
				throw new RuntimeException("Bean not found " + name);
			if (beans.size() > 1)
				throw new RuntimeException("Ambiguous beans found " + name);
			Bean<?> bean = beans.iterator().next();
			CreationalContext<?> cc = beanManager.createCreationalContext(bean);
			return (T)beanManager.getReference(bean, Object.class, cc);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T byType(Class<T> type, Context context) {
			Set<Bean<?>> beans = beanManager.getBeans(type);
			if (beans.size() == 0)
				throw new RuntimeException("Bean not found " + type);
			if (beans.size() > 1)
				throw new RuntimeException("Ambiguous beans found " + type);
			Bean<?> bean = beans.iterator().next();
			CreationalContext<?> cc = beanManager.createCreationalContext(bean);
			return (T)beanManager.getReference(bean, type, cc);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] allByType(Class<T> type, Context context) {
			Set<Bean<?>> beans = beanManager.getBeans(type);
			T[] instances = (T[])Array.newInstance(type, beans.size());
			int i = 0;
			for (Bean<?> bean : beans) {
				CreationalContext<?> cc = beanManager.createCreationalContext(bean);
				instances[i++] = (T)beanManager.getReference(bean, type, cc);
			}
			return instances;
		}

		@Override
		public List<String> allNames() {
			Set<Bean<?>> beans = beanManager.getBeans(Object.class);
			List<String> names = new ArrayList<String>();
			for (Bean<?> bean : beans) {
				if (bean.getName() != null)
					names.add(bean.getName());
			}
			return names;
		}
		
		@Override
		public void set(String name, Object instance) {
			// Nothing, managed by CDI
		}

		@Override
		public void set(Object instance) {
			// Nothing, managed by CDI
		}

		@Override
		public void remove(String name) {
			// Nothing, managed by CDI
		}
		
		@Override
		public void clear() {
			// Nothing, managed by CDI
		}
	}
}
