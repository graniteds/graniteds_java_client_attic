/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

import java.util.Map;

import org.granite.client.messaging.channel.ResponseMessageFuture;
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
public class PagedQuery<E> extends PagedCollection<E> implements Component, PropertyHolder, NameAware, ContextAware, Initializable {
    
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger("org.granite.tide.collections.javafx.PagedQuery");
	
    protected ComponentImpl component = null;
    
    private final ServerSession serverSession;
    private String remoteComponentName = null;
    private Context context = null;
	
    protected String methodName = "find";
    protected boolean methodNameSet = false;
    
    private Object filter = null;
	
    
    public PagedQuery(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
	
    public void setName(String componentName) {
    	remoteComponentName = componentName;
    }
    
    public void setContext(Context context) {
    	this.context = context;
    	if (component != null)
    		component.setContext(context);
    }
    
	public void init() {
		component = new ComponentImpl(serverSession);
		component.setName(remoteComponentName);
		component.setContext(context);
		// filter.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler);
	}
	
	public Object getFilter() {
		return filter;
	}
	public void setFilter(Object filter) {
		this.filter = filter;
//		if (_filter != null)
//			_filter.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler);
//		
//		if (filter is IPropertyChangeNotifier) {
//			_internalFilter = filter;
//			_filter = IPropertyChangeNotifier(filter);
//		}
//		else {
//			_internalFilter = filter;
//			_filter = new ObjectProxy(_internalFilter);
//		}
//		_filter.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler, false, 0, true);
	}
	public void setFilterClass(Class<?> filterClass) throws IllegalAccessException, InstantiationException {
		filter = TypeUtil.newInstance(filterClass, Object.class);
	}


	public String getName() {
	    return remoteComponentName;
	}

	public void setRemoteComponentName(String remoteComponentName) {
		if (remoteComponentName != this.remoteComponentName) {
			Object component = context.byName(remoteComponentName);
			if (component == null || !(component instanceof ComponentImpl)) {
				this.component = new ComponentImpl(serverSession);
				this.component.setName(remoteComponentName);
				this.component.setContext(context);
				context.set(remoteComponentName, component);
			}
		}
		else {
			this.component = new ComponentImpl(serverSession);
			this.component.setName(remoteComponentName);
			this.component.setContext(context);
		}
	}
	
	public void setRemoteComponentClass(Class<? extends ComponentImpl> remoteComponentClass) throws IllegalAccessException, InstantiationException {
		component = TypeUtil.newInstance(remoteComponentClass, new Class<?>[] { ServerSession.class }, new Object[] { serverSession });
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
		this.methodNameSet = true;
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
		
		PagedCollectionResponder findResponder = new PagedCollectionResponder(serverSession, first, max);		
		Object filter = this.filter;
		
		doFind(filter, first, max, null, findResponder);
	}
	
	protected void doFind(Object filter, int first, int max, Object sort, PagedCollectionResponder findResponder) { 			
		// Force evaluation of max, results and count
		String[] order = new String[0];
		boolean[] desc = new boolean[0];
//		if (this.multipleSort) {
//			if (sort != null) {
//				order = new Array();
//				desc = new Array();
//				for each (var s:SortField in sort.fields) {
//					order.push(s.name);
//					desc.push(s.descending);
//				}
//			}
//		}
//		else {
//			order = sort != null && sort.fields.length > 0 ? sort.fields[0].name : null;
//			desc = sort != null && sort.fields.length > 0 ? sort.fields[0].descending : false;
//		}
		
		component.call(methodName, new Object[] { 
			filter, first, max, order, desc, 
			findResponder }
		);
	}
	
	@Override
	protected Map<String, Object> getResult(TideResultEvent<?> event, int first, int max) {
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>)event.getResult();
    	if (!result.containsKey("firstResult"))
    		result.put("firstResult", first);
    	if (!result.containsKey("maxResults"))
    		result.put("maxResults", max);
	    return result;
	}
	
	
//	private function filterChangedHandler(event:Event):void {
//	    _fullRefresh = true;
//	    _filterRefresh = true;
//	}
	
	
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
	public ResponseMessageFuture call(String operation, Object... args) {
		throw new UnsupportedOperationException();
	}
	
}
