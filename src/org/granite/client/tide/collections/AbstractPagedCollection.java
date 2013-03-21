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

package org.granite.client.tide.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.granite.client.tide.collections.javafx.Sort;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.tide.server.TideRpcEvent;
import org.granite.logging.Logger;
import org.granite.tide.data.model.Page;

/**
 * @author William DRAI
 */
public abstract class AbstractPagedCollection<E> implements List<E>, TideEventObserver {
	
    private static final Logger log = Logger.getLogger(AbstractPagedCollection.class);
    
	
	/**
	 * 	@private
	 */
    protected boolean initializing = false;
    private boolean initSent = false;
    
	/**
	 * 	@private
	 */
	protected int first;
	/**
	 * 	@private
	 */
    protected int last;			// Current last index of local data
	/**
	 * 	@private
	 */
    protected int max;           // Page size
	/**
	 * 	@private
	 */
    protected int count;         // Result count
    private E[] localIndex = null;
    
	/**
	 * 	@private
	 */
	protected boolean fullRefresh;
	/**
	 * 	@private
	 */
	protected boolean filterRefresh = false;
	

	protected Sort sort = null;
	
	public void setSort(Sort sort) {
		this.sort = sort;
	}
	
	public Sort getSort() {
		return sort;
	}
	
	
	public AbstractPagedCollection() {
		super();
	    log.debug("create collection");
		first = 0;
		last = 0;
		count = 0;
		initializing = true;
	}
	
	
	/**
	 *	Get total number of elements
	 *  
	 *  @return collection total length
	 */
	@Override
	public int size() {
		if (initialFind())
	        return 0;
	    else if (localIndex == null)
	        return 0;
		return count;
	}
	
	/**
	 *  Set the page size. The collection will store in memory twice this page size, and each server call
	 *  will return at most the page size.
	 * 
	 *  @param max maximum number of requested elements
	 */
	public void setMaxResults(int max) {
		this.max = max;
	}
	
	
	private Class<? extends E> elementClass;
	private String elementName;	
	private Set<String> entityNames = new HashSet<String>();
	
	public void setElementClass(Class<? extends E> elementClass) {
		this.elementClass = elementClass;
		
		if (this.elementName != null)
			entityNames.remove(elementName);
		
		elementName = elementClass != null ? elementClass.getSimpleName() : null;
		
		if (this.elementName != null)
			entityNames.add(this.elementName);
	}

	@Override
	public void handleEvent(TideEvent event) {
		if (event.getType().startsWith(UpdateKind.REFRESH.eventName() + ".")) {
			String entityName = event.getType().substring(UpdateKind.REFRESH.eventName().length()+1);
			if (entityNames.contains(entityName))
				fullRefresh();
		}
	}	
	
	
	/**
	 * 	Clear collection content
	 */
	@Override
	public void clear() {
		initializing = true;
		initSent = false;
		getWrappedList().clear();
		clearLocalIndex();
		first = 0;
		last = first+max;
		// sort = null;
	}
	
	
	private List<Integer[]> pendingRanges = new ArrayList<Integer[]>();
	
	/**
	 *	Abstract method: trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	protected void find(int first, int last) {
		log.debug("find from %d to %d", first, last);
		
		pendingRanges.add(new Integer[] { first, last });
	}
	
	
	/**
	 *	Force refresh of collection when filter/sort have been changed
	 * 
	 *  @return always false
	 */
	public boolean fullRefresh() {
	    this.fullRefresh = true;
	    return refresh();
	}
	
	/**
	 *	Refresh collection with new filter/sort parameters
	 * 
	 *  @return always false
	 */
	public boolean refresh() {
		// Recheck sort fields to listen for asc/desc change events
		pendingRanges.clear();
		
		if (fullRefresh) {
			log.debug("full refresh");
			
			clearLocalIndex();
			
			fullRefresh = false;
			if (filterRefresh) {
			    first = 0;
			    last = first+max;
			    filterRefresh = false;
			}
        }
        else
			log.debug("refresh");			
        
		if (!initialFind())
			find(first, last);
		return true;
	}
	
	private boolean initialFind() {
		if (max > 0 && !initializing)
			return false;
		
		if (!initSent) {
			log.debug("initial find");
			find(0, max);
			initSent = true;
		}
		return true;
	}
	
	private void clearLocalIndex() {
		localIndex = null;
	}
	
	/**
	 *  Build a result object from the result event
	 *  
	 *  @param event the result event
	 *  @param first first index requested
	 *  @param max max elements requested
	 *   
	 *  @return an object containing data from the collection
	 *      resultList   : the retrieved data
	 *      resultCount  : the total count of elements (non paged)
	 *      firstResult  : the index of the first retrieved element
	 *      maxResults   : the maximum count of retrieved elements 
	 */
	protected abstract Page<E> getResult(TideResultEvent<?> event, int first, int max);
	
	
	/**
	 *  Notify listeners of remote page result
	 *  
	 *  @param event the remote event (ResultEvent or FaultEvent)
	 */
	protected abstract void firePageChange(TideRpcEvent event);
	
	
	/**
	 * 	@private
	 *  Initialize collection after first find
	 *   
	 *  @param the result event of the first find
	 */
	protected void initialize(TideResultEvent<?> event) {
	}
	
	/**
	 * 	@private
	 *	Event handler for results query
	 * 
	 *  @param event the result event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	protected void findResult(TideResultEvent<?> event, int first, int max) {
	    Page<E> result = getResult(event, first, max);
	    
	    handleResult(result, event, first, max);
	}
	
	/**
	 * 	@private
	 *	Event handler for results query
	 * 
	 *  @param result the result object
	 *  @param event the result event
	 */
	@SuppressWarnings("unchecked")
	protected void handleResult(Page<E> result, TideResultEvent<?> event, int first, int max) {
		List<E> list = (List<E>)result.getResultList();

		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				break;
			}
		}
		
		if (initializing && event != null) {
			if (this.max == 0 && result.getMaxResults() > 0)
		    	this.max = result.getMaxResults();
		    initialize(event);
		}
		
		int nextFirst = (Integer)result.getFirstResult();
		int nextLast = nextFirst + (Integer)result.getMaxResults();
		
		int page = max > 0 ? nextFirst / max : 0;
		log.debug("handle result page %d (%d - %d)", page, nextFirst, nextLast);
		
		count = result.getResultCount();
		
	    initializing = false;
		
	    if (localIndex != null) {
	    	List<String> entityNames = new ArrayList<String>();
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName))
					entityNames.remove(entityName);
	        }
	    }
	    for (Object o : list) {
	    	if (elementClass == null || (o != null && o.getClass().isAssignableFrom(elementClass)))
	    		elementClass = (Class<? extends E>)o.getClass();
	    }
	    localIndex = (E[])Array.newInstance(elementClass, list.size());
		localIndex = list.toArray(localIndex);
	    if (localIndex != null) {
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName))
					entityNames.add(entityName);
	        }
	    }
	    
		// Must be before collection event dispatch because it can trigger a new getItemAt
		this.first = nextFirst;
		this.last = nextLast;
	    
		pendingRanges.clear();
		
		firePageChange(event);
	}
	
	/**
	 *  @private
	 *	Event handler for results fault
	 *  
	 *  @param event the fault event
	 *  @param first first requested index
	 *  @param max max elements requested
	 */
	protected void findFault(TideFaultEvent event, int first, int max) {
		handleFault(event);
	}
	
	/**
	 * 	@private
	 *	Event handler for results query fault
	 * 
	 *  @param event the fault event
	 */
	protected void handleFault(TideFaultEvent event) {
		log.debug("findFault: %s", event);
		
		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				break;
			}
		}
	    
		firePageChange(event);
	}
	
	
	protected abstract List<E> getInternalWrappedList();
	
	protected abstract List<E> getWrappedList();
	
	
	/**
	 * 	Override of getItemAt with ItemPendingError management
	 * 
	 *	@param index index of requested item
	 *	@param prefetch not used
	 *  @return object at specified index
	 */
	@Override
	public E get(int index) {
		if (index < 0)
			return null;
	
		if (initialFind())
			return null;

		if (localIndex != null && index >= first && index < last) {	// Local data available for index
		    int j = index-first;
		    if (j >= 0 && j < localIndex.length)
		    	return localIndex[j];
		    // Index not in current loaded range, max is more than last page size
		    return null;
		}
		
		// If already in a pending range, return null
		for (Integer[] pendingRange : pendingRanges) {
			if (index >= pendingRange[0] && index < pendingRange[1])
				return null;
		}
	    
	    int page = index / max;
	    
		// Trigger a results query for requested page
		int nfi = 0;
		int nla = 0;
		@SuppressWarnings("unused")
		int idx = page * max;
		if (index >= last && index < last + max) {
			nfi = first;
			nla = last + max;
			if (nla > nfi + 2*max)
			    nfi = nla - 2*max;
			if (nfi < 0)
			    nfi = 0;
			if (nla > count)
			    nla = count;
		}
		else if (index < first && index >= first - max) {
			nfi = first - max;
			if (nfi < 0)
				nfi = 0;
			nla = last;
			if (nla > nfi + 2*max)
			    nla = nfi + 2*max;
			if (nla > count)
			    nla = count;
		}
		else {
			nfi = index - max;
			nla = nfi + 2 * max;
			if (nfi < 0)
				nfi = 0;
			if (nla > count)
			    nla = count;
		}
		log.debug("request find for index " + index);
		find(nfi, nla);
		return null;
	}
	
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}


	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;
		
		if (localIndex != null) {
			for (Object obj : localIndex) {
				if (o.equals(obj))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return false;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null)
			return -1;
		
		if (localIndex != null) {
			for (int i = 0; i < localIndex.length; i++) {
				if (o.equals(localIndex[i]))
					return first+i;;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null)
			return -1;
				
		if (localIndex != null) {
			int index = -1;
			for (int i = 0; i < localIndex.length; i++) {
				if (o.equals(localIndex[i]))
					index = first+i;;
			}
			return index;
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return new PagedCollectionIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		return new PagedCollectionIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new PagedCollectionIterator();
	}
	
	
	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
    
//    protected void itemUpdateHandler(PropertyChangeEvent event) {
//		if (hasEventListener(CollectionEvent.COLLECTION_CHANGE)) {
//	        var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE);
//	        ce.kind = CollectionEventKind.UPDATE;
//	        ce.items.push(event);
//	        ce.location = -1;
//	        dispatchEvent(ce);
//	    }
//    }

	
	public static class SortField {
		private String name;
		private boolean direction;
		
		public SortField(String name, boolean direction) {
			this.name = name;
			this.direction = direction;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean getDirection() {
			return direction;
		}
	}
	
	public class PagedCollectionIterator implements ListIterator<E> {
		
		private ListIterator<E> wrappedListIterator;
		
		public PagedCollectionIterator() {
			wrappedListIterator = getWrappedList().listIterator();
		}

		public PagedCollectionIterator(int index) {
			wrappedListIterator = getWrappedList().listIterator(index);
		}

		@Override
		public boolean hasNext() {
			return wrappedListIterator.hasNext();
		}
	
		@Override
		public E next() {
			return wrappedListIterator.next();
		}
	
		@Override
		public boolean hasPrevious() {
			return wrappedListIterator.hasPrevious();
		}
	
		@Override
		public E previous() {
			return wrappedListIterator.previous();
		}
	
		@Override
		public int nextIndex() {
			return wrappedListIterator.nextIndex();
		}
	
		@Override
		public int previousIndex() {
			return wrappedListIterator.previousIndex();
		}
	
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public void set(E e) {
			throw new UnsupportedOperationException();
		}
	
		@Override
		public void add(E e) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	public class PagedCollectionResponder implements TideResponder<Object> {
	    
		private ServerSession serverSession;
	    private int first;
	    private int max;
	    
	    
	    public PagedCollectionResponder(ServerSession serverSession, int first, int max) {
	    	this.serverSession = serverSession;
	        this.first = first;
	        this.max = max;
	    }
	    
	    @Override
    	@SuppressWarnings("unchecked")
	    public void result(TideResultEvent<Object> event) {
	    	Object result = event.getResult();
	    	
	    	List<E> list = null;
	    	int first, max;
	    	
	    	if (result instanceof Map<?, ?>) {
	    		Map<String, Object> map = (Map<String, Object>)result;
	    		list = (List<E>)map.get("resultList");
	    		first = (Integer)map.get("firstResult");
	    		max = (Integer)map.get("maxResults");
	    	}
	    	else {
	    		Page<E> page = (Page<E>)result;
	    		list = page.getResultList();
	    		first = page.getFirstResult();
	    		max = page.getMaxResults();
	    	}
	    	
	    	EntityManager entityManager = event.getContext().getEntityManager();
	    	
	    	if (!initializing) {
	    		// Adjust internal list to expected results without triggering events	    		
		    	if (first > AbstractPagedCollection.this.first && first < AbstractPagedCollection.this.last) {
	    			getInternalWrappedList().subList(0, first - AbstractPagedCollection.this.first).clear();
		    		for (int i = 0; i < first - AbstractPagedCollection.this.first && AbstractPagedCollection.this.last - first + i < list.size(); i++)
		    			getInternalWrappedList().add((E)entityManager.mergeExternalData(serverSession, list.get(AbstractPagedCollection.this.last - first + i)));
		    	}
		    	else if (first+max > AbstractPagedCollection.this.first && first+max < AbstractPagedCollection.this.last) {
		    		if (first+max-AbstractPagedCollection.this.first < getWrappedList().size())
		    			getInternalWrappedList().subList(first+max-AbstractPagedCollection.this.first, getWrappedList().size()).clear();
		    		else
		    			getInternalWrappedList().clear();
		    		for (int i = 0; i < AbstractPagedCollection.this.first - first && i < list.size(); i++)
		    			getInternalWrappedList().add(i, (E)entityManager.mergeExternalData(serverSession, list.get(i)));
		    	}
		    	else if (first >= AbstractPagedCollection.this.last || first+max <= AbstractPagedCollection.this.first) {
		    		getInternalWrappedList().clear();
		    		for (int i = 0; i < list.size(); i++)
		    			getInternalWrappedList().add((E)entityManager.mergeExternalData(serverSession, list.get(i)));
		    	}
	    	}
	    	
    		entityManager.mergeExternalData(serverSession, list, getWrappedList(), null, null, null);
	    	
            findResult(event, first, max);
	    }
	    
	    public void fault(TideFaultEvent event) {
            findFault(event, first, max);
	    } 
	}
}
