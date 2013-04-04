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

package org.granite.client.tide.collections.javafx;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.Initializable;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;
import org.granite.tide.data.model.Page;
import org.granite.tide.data.model.PageInfo;
import org.granite.util.TypeUtil;

/**
 * 	Implementation of the Tide paged collection with an generic service backend.<br/>
 *  <br/>
 *  By default the corresponding service should have the same name and expose a 'find' method<br/>
 *  that returns a Map with the following properties :<br/>
 *  <pre>
 *  resultCount
 *  resultList
 *  firstResult
 *  maxResults
 *  </pre>
 * 
 *  The name of the remote service can be overriden by setting the remoteComponentName property.
 *  The name of the remote method can by set by the remoteMethodName property.
 * 
 * 	@author William DRAI
 */
public class PagedQuery<E, F> extends PagedCollection<E> implements Component, PropertyHolder, NameAware, ContextAware, Initializable {
    
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger("org.granite.client.tide.collections.javafx.PagedQuery");
	
    protected Component component = null;
    
    private final ServerSession serverSession;
    private String remoteComponentName = null;
    private Context context = null;
	
    protected String methodName = "find";
    protected boolean methodNameSet = false;
    
    protected boolean usePage = false;
    
    private ObservableMap<String, Object> filterMap = FXCollections.observableMap(new ConcurrentHashMap<String, Object>());
    private ObjectProperty<F> filter = null;
	
    
    public PagedQuery(ServerSession serverSession) {
    	this.serverSession = serverSession;
    	
    	this.filterMap.addListener(new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends String, ?> change) {
				fullRefresh = true;
				filterRefresh = true;
			}
    	});
    }
	
    public void setName(String componentName) {
    	remoteComponentName = componentName;
    }
    
    public void setContext(Context context) {
    	this.context = context;
    	if (component instanceof ContextAware)
    		((ContextAware)component).setContext(context);
    }
    
	public void init() {
		component = new ComponentImpl(serverSession);
		((ComponentImpl)component).setName(remoteComponentName);
		((ComponentImpl)component).setContext(context);
	}
	
	public ObjectProperty<F> filterProperty() {
		return filter;
	}
	public F getFilter() {
		return filter != null ? filter.get() : null;
	}
	public Map<String, Object> getFilterMap() {
		return filterMap;
	}
	public void setFilter(F filter) {
		this.filter.set(filter);
	}
	@SuppressWarnings("unchecked")
	public void setFilterClass(Class<F> filterClass) throws IllegalAccessException, InstantiationException {
		this.filter = new SimpleObjectProperty<F>(this, "filter");
		setFilter((F)TypeUtil.newInstance(filterClass, Object.class));
	}
	
	@Override
	public boolean refresh() {
		if (getFilter() != null && this.context.getEntityManager().isDeepDirtyEntity(getFilter())) {
			filterRefresh = true;
			fullRefresh = true;
		}
		return super.refresh();
	}
	

	public String getName() {
	    return remoteComponentName;
	}

	public void setRemoteComponentName(String remoteComponentName) {
		if (remoteComponentName != this.remoteComponentName) {
			Object component = context.byName(remoteComponentName);
			if (component == null || !(component instanceof ComponentImpl)) {
				this.component = new ComponentImpl(serverSession);
				((ComponentImpl)this.component).setName(remoteComponentName);
				((ComponentImpl)this.component).setContext(context);
				context.set(remoteComponentName, component);
			}
		}
		else {
			this.component = new ComponentImpl(serverSession);
			((ComponentImpl)this.component).setName(remoteComponentName);
			((ComponentImpl)this.component).setContext(context);
		}
	}
	
	public void setRemoteComponentClass(Class<? extends ComponentImpl> remoteComponentClass) throws IllegalAccessException, InstantiationException {
		component = TypeUtil.newInstance(remoteComponentClass, new Class<?>[] { ServerSession.class }, new Object[] { serverSession });
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
		this.methodNameSet = true;
	}
	
	public void setUsePage(boolean usePage) {
		this.usePage = usePage;
	}
	
	
	/**
	 *	Trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	protected void find(int first, int last) {
		super.find(first, last);
		
		int max = 0;
		if (this.initializing && this.max > 0)
			max = this.max;
		else if (!this.initializing)
		    max = last-first;
		
		PagedCollectionResponder findResponder = new PagedCollectionResponder(first, max);		
		Object filter = this.filter != null ? getFilter() : filterMap;
		
		doFind(filter, first, max, findResponder);
	}
	
	protected synchronized void doFind(Object filter, int first, int max, PagedCollectionResponder findResponder) {
		// Force evaluation of max, results and count
		String[] order = new String[0];
		boolean[] desc = new boolean[0];
		if (sort != null) {
			sort.build();
			order = sort.getOrder();
			desc = sort.getDesc();
			if (order.length == 0)
				order = null;
			if (desc.length == 0)
				desc = null;
		}
		
		boolean usePage = this.usePage;
		try {
			for (Method m : component.getClass().getMethods()) {
				if (m.getName().equals(methodName) && m.getParameterTypes().length >= 2 
						&& PageInfo.class.isAssignableFrom(m.getParameterTypes()[1])) {
					usePage = true;
					break;
				}
			}
		}
		catch (Exception e) {
			// Untyped component proxy
		}
		
		if (usePage) {
			PageInfo pageInfo = new PageInfo(first, max, order, desc);
			component.call(methodName, new Object[] { filter, pageInfo, findResponder });
		}
		else {
			component.call(methodName, new Object[] { filter, first, max, order, desc, findResponder });
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Page<E> getResult(TideResultEvent<?> event, int first, int max) {
		if (event.getResult() instanceof Page<?>)
			return (Page<E>)event.getResult();
		
		Map<String, Object> result = (Map<String, Object>)event.getResult();
		Page<E> page = new Page<E>(result.containsKey("firstResult") ? (Integer)result.get("firstResult") : first, 
				result.containsKey("maxResults") ? (Integer)result.get("maxResults") : max,
				((Number)result.get("resultCount")).intValue(), (List<E>)result.get("resultList"));
	    return page;
	}
	
	
	/**
	 * PropertyHolder interface
	 */
	public Object getObject() {
		if (component instanceof PropertyHolder)
	    	return ((PropertyHolder)component).getObject();
	    return null;
	}
	
    public void setProperty(String propName, Object value) {
    	if (component instanceof PropertyHolder)
    		((PropertyHolder)component).setProperty(propName, value);
    }

	
	@Override
	public <T> Future<T> call(String operation, Object... args) {
		throw new UnsupportedOperationException();
	}
	
}
