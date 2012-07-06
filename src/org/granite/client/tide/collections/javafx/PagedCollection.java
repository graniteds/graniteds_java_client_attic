package org.granite.client.tide.collections.javafx;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.events.TideEvent;
import org.granite.client.tide.events.TideEventObserver;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideMergeResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.util.javafx.ListListenerHelper;
import org.granite.logging.Logger;


public abstract class PagedCollection<E> implements ObservableList<E>, TideEventObserver {
	
    private static final Logger log = Logger.getLogger(PagedCollection.class);
    
    
    public static final String COLLECTION_PAGE_CHANGE = "collectionPageChange";
    public static final String RESULT = "result";
    public static final String FAULT = "fault";
    
	
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
    
    private List<E> internalWrappedList = new ArrayList<E>();
    protected ObservableList<E> wrappedList;
    
	/**
	 * 	@private
	 */
	protected boolean fullRefresh;
	/**
	 * 	@private
	 */
	protected boolean filterRefresh = false;
	private List<Object[]> ipes;		// Array of ItemPendingErrors
	
	// GDS-523
	@SuppressWarnings("unused")
	private String uidProperty = "uid";
	
	public void setUidProperty(String uidProperty) {
		this.uidProperty = uidProperty;
	}
	
	// GDS-712
	@SuppressWarnings("unused")
	private boolean multipleSort = true;
	
	public void setMultipleSort(boolean multipleSort) {
		this.multipleSort = multipleSort;
	}
	
	
	public PagedCollection() {
		super();
	    log.debug("create collection");
		ipes = null;
		first = 0;
		last = 0;
		count = 0;
		initializing = true;
		
		wrappedList = FXCollections.observableList(internalWrappedList);
		wrappedList.addListener(new WrappedListListChangeListener());
	}
	
	
	/**
	 *	Get total number of elements
	 *  
	 *  @return collection total length
	 */
	@Override
	public int size() {
	    if (initializing) {
	    	if (!initSent) {
	    		log.debug("initial find");
		    	find(0, max);
		    	initSent = true;
		    }
	        return 0;
	    }
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
		if (event.getType().startsWith("org.granite.tide.data.refresh.")) {
			String entityName = event.getType().substring("org.granite.tide.data.refresh.".length());
			if (entityNames.contains(entityName))
				fullRefresh();
		}
	}	
	
	
	/**
	 * 	Clear collection content
	 */
	public void clear() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("resultList", new ArrayList<E>());
		result.put("resultCount", 0);
		result.put("firstResult", 0);
		result.put("maxResults", max);
		handleResult(result, null, 0, 0);
		initializing = true;
		initSent = false;
		clearLocalIndex();
		first = 0;
		last = first+max;
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
        
		find(first, last);
		return true;
	}
	
	private void clearLocalIndex() {
		// Force complete refresh after changing sorting or filtering
		if (localIndex != null) {
			for (int i = 0; i < localIndex.length; i++)
				stopTrackUpdates(localIndex[i]);
		}
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
	protected abstract Map<String, Object> getResult(TideResultEvent<?> event, int first, int max);
	
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
	    Map<String, Object> result = getResult(event, first, max);
	    
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
	protected void handleResult(Map<String, Object> result, TideResultEvent<?> event, int first, int max) {
		List<E> list = (List<E>)result.get("resultList");

		for (Iterator<Integer[]> ipr = pendingRanges.iterator(); ipr.hasNext(); ) {
			Integer[] pr = ipr.next();
			if (pr[0] == first && pr[1] == first+max) {
				ipr.remove();
				break;
			}
		}
		
		if (initializing && event != null) {
			if (max == 0 && result.containsKey("maxResults"))
		    	max = (Integer)result.get("maxResults");
		    initialize(event);
		}
		
		int nextFirst = (Integer)result.get("firstResult");
		int nextLast = nextFirst + (Integer)result.get("maxResults");
		
		int page = nextFirst / max;
		log.debug("handle result page %d (%d - %d)", page, nextFirst, nextLast);
		
		count = ((Number)result.get("resultCount")).intValue();
		
	    initializing = false;
		
	    if (localIndex != null) {
	    	List<String> entityNames = new ArrayList<String>();
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName))
					entityNames.remove(entityName);

	            stopTrackUpdates(localIndex[i]);
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
					
	            startTrackUpdates(localIndex[i]);
	        }
	    }
	    
		// Must be before collection event dispatch because it can trigger a new getItemAt
		this.first = nextFirst;
		this.last = nextLast;
	    
		pendingRanges.clear();
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
	    
//    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, FAULT, -1, -1, [ event ]));
	}
	
	
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
	
		// log.debug("get item at %d", index);
		
		if (max == 0 || initializing) {
			if (!initSent) {
				log.debug("initial find");
			    find(0, max);
			    initSent = true;
			}
		    return null;
		}

		if (localIndex != null && index >= first && index < last) {	// Local data available for index
		    int j = index-first;
			return localIndex[j];
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
	
	
    protected void startTrackUpdates(E item) {
//        if (item != null)
//            IEventDispatcher(item).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler, false, 0, true);
    }
    
    protected void stopTrackUpdates(E item) {
//        if (item != null)
//            IEventDispatcher(item).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler);    
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
	
	
	private ListListenerHelper<E> helper = new ListListenerHelper<E>();
	
	public void addListener(ListChangeListener<? super E> listener) {
		helper.addListener(listener);
    }

	public void removeListener(ListChangeListener<? super E> listener) {
		helper.removeListener(listener);
    }
	
	public void addListener(InvalidationListener listener) {
		helper.addListener(listener);
    }

	public void removeListener(InvalidationListener listener) {
		helper.removeListener(listener);
    }
	
	public class WrappedListListChangeListener implements ListChangeListener<E> {		
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends E> change) {
	    	ListChangeWrapper wrappedChange = new ListChangeWrapper(wrappedList, change);
    		helper.fireValueChangedEvent(wrappedChange);
	    }
	}

	public class ListChangeWrapper extends ListChangeListener.Change<E> {            
	    private final ListChangeListener.Change<? extends E> wrappedChange;
	    
	    public ListChangeWrapper(ObservableList<E> list, ListChangeListener.Change<? extends E> wrappedChange) {
	        super(list);
	        this.wrappedChange = wrappedChange;
	    }

	    @Override
		public int getAddedSize() {
			return wrappedChange.getAddedSize();
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<E> getAddedSubList() {
			return (List<E>)wrappedChange.getAddedSubList();
		}

		@Override
		public int getRemovedSize() {
			return wrappedChange.getRemovedSize();
		}

		@Override
		public boolean wasAdded() {
			return wrappedChange.wasAdded();
		}

		@Override
		public boolean wasPermutated() {
			return wrappedChange.wasPermutated();
		}

		@Override
		public boolean wasRemoved() {
			return wrappedChange.wasRemoved();
		}

		@Override
		public boolean wasReplaced() {
			return wrappedChange.wasReplaced();
		}

		@Override
		public boolean wasUpdated() {
			return wrappedChange.wasUpdated();
		}

		@Override
	    public int getFrom() {
			int from = wrappedChange.getFrom();
	        return from+first;
	    }

	    @Override
	    public int getTo() {
	    	int to = wrappedChange.getTo();
	        return to+first;
	    }

	    @Override
	    protected int[] getPermutation() {
	        // TODO
	        return new int[0]; // wrappedChange.getPermutation();
	    }

	    @Override
	    public int getPermutation(int num) {
	        return wrappedChange.getPermutation(num);
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public List<E> getRemoved() {
	        return (List<E>)wrappedChange.getRemoved();
	    }

	    @Override
	    public boolean next() {
	        return wrappedChange.next();
	    }

	    @Override
	    public void reset() {
	        wrappedChange.reset();
	    }        
	}
	
	
	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
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
	public boolean addAll(E... arg0) {
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
	public void remove(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(E... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(E... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setAll(Collection<? extends E> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setAll(E... arg0) {
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

	
	public class PagedCollectionIterator implements ListIterator<E> {
		
		private ListIterator<E> wrappedListIterator;
		
		public PagedCollectionIterator() {
			wrappedListIterator = wrappedList.listIterator();
		}

		public PagedCollectionIterator(int index) {
			wrappedListIterator = wrappedList.listIterator(index);
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
	
	
	public class PagedCollectionResponder implements TideMergeResponder<Map<String, Object>> {
	    
		private ServerSession serverSession;
	    private int first;
	    private int max;
	    private Map<String, Object> mergedResult;
	    
	    
	    public PagedCollectionResponder(ServerSession serverSession, int first, int max) {
	    	this.serverSession = serverSession;
	        this.first = first;
	        this.max = max;
	        this.mergedResult = new HashMap<String, Object>();
	    }
	    
	    @Override
    	@SuppressWarnings("unchecked")
	    public void result(TideResultEvent<Map<String, Object>> event) {
			List<E> list = (List<E>)mergedResult.get("resultList");
	    	
	    	int first = (Integer)mergedResult.get("firstResult");
	    	int max = (Integer)mergedResult.get("maxResults");
	    	
	    	EntityManager entityManager = event.getContext().getEntityManager();
	    	
	    	if (!initializing) {
	    		// Adjust internal list to expected results without triggering events	    		
		    	if (first > PagedCollection.this.first && first < PagedCollection.this.last) {
	    			internalWrappedList.subList(0, first - PagedCollection.this.first).clear();
		    		for (int i = 0; i < first - PagedCollection.this.first && PagedCollection.this.last - first + i < list.size(); i++)
		    			internalWrappedList.add((E)entityManager.mergeExternalData(serverSession, list.get(PagedCollection.this.last - first + i)));
		    	}
		    	else if (first+max > PagedCollection.this.first && first+max < PagedCollection.this.last) {
		    		internalWrappedList.subList(first+max-PagedCollection.this.first, wrappedList.size()).clear();
		    		for (int i = 0; i < PagedCollection.this.first - first && i < list.size(); i++)
		    			internalWrappedList.add(i, (E)entityManager.mergeExternalData(serverSession, list.get(i)));
		    	}
		    	else if (first >= PagedCollection.this.last || first+max <= PagedCollection.this.first) {
		    		internalWrappedList.clear();
		    		for (int i = 0; i < list.size(); i++)
		    			internalWrappedList.add((E)entityManager.mergeExternalData(serverSession, list.get(i)));
		    	}
	    	}
	    	
    		entityManager.mergeExternalData(serverSession, list, wrappedList, null, null);
	    	
            findResult(event, first, max);
	    }
	    
	    public void fault(TideFaultEvent event) {
            findFault(event, first, max);
	    } 

	    @Override
        public Map<String, Object> getMergeResultWith() {
            return mergedResult;
        }
	}
}
