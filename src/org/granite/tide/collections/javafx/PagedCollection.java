package org.granite.tide.javafx;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.event.Event;

import org.granite.logging.Logger;
import org.granite.tide.Context;
import org.granite.tide.rpc.ServerSession;
import org.granite.tide.rpc.TideFaultEvent;
import org.granite.tide.rpc.TideResultEvent;


public abstract class PagedCollection<T> implements ObservableList<T> {
	
    private static final Logger log = Logger.getLogger(PagedCollection.class);
    
    
    public static final String COLLECTION_PAGE_CHANGE = "collectionPageChange";
    public static final String RESULT = "result";
    public static final String FAULT = "fault";
    
	
	/**
	 * 	@private
	 */
    protected ServerSession serverSession = null;
	protected String componentName = null;
	protected Context context = null;
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
    private List<T> list = null;
    private T[] localIndex = null;
    
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
	
	
	public PagedCollection(ServerSession serverSession) {
		super();
	    log.debug("create collection");
		this.serverSession = serverSession;
		ipes = null;
		first = 0;
		last = 0;
		count = 0;
		list = null;
		initializing = true;
	}
	
	
	public void init(String componentName, Context context) {
		this.componentName = componentName;
		this.context = context;
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
	
	
	private Class<? extends T> elementClass;
	private String elementName;
	
	public void setElementClass(Class<? extends T> elementClass) {
		this.elementClass = elementClass;
		
		// TODO
//		if (this.elementName != null)
//        	context.removeEventListener("org.granite.tide.data.refresh." + elementName, refreshHandler);
		
		elementName = elementClass != null ? elementClass.getSimpleName() : null;
		
		// TODO
//		if (this.elementName != null)
//        	context.addEventListener("org.granite.tide.data.refresh." + elementName, refreshHandler, false, 0, true);
	}
	
	
	
	/**
	 * 	Clear collection content
	 */
	public void clear() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("resultList", new ArrayList<T>());
		result.put("resultCount", 0);
		result.put("firstResult", 0);
		result.put("maxResults", max);
		handleResult(result, null);
		initializing = true;
		initSent = false;
		clearLocalIndex();
		first = 0;
		last = first+max;
	}
	
	
	/**
	 *	Abstract method: trigger a results query for the current filter
	 *	@param first	: index of first required result
	 *  @param last     : index of last required result
	 */
	protected void find(int first, int last) {
		log.debug("find from %d to %d", first, last);
		
//		if (beforeFindCall != null)
//			beforeFindCall(first, last);
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
		// (AdvancedDataGrid does not dispatch sortChanged when only asc/desc is changed)
//		if (sort != null) {
//			sort.compareFunction = nullCompare;
//			if (sort.fields != null && sort.fields.length != _sortFieldListenersSet) {
//				for each (var field:SortField in sort.fields)
//					field.addEventListener("descendingChanged", sortFieldChangeHandler, false, 0, true);
//				_sortFieldListenersSet = sort.fields.length;
//			}
//		}
		
		// _ipes = null;
		
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

	
	@SuppressWarnings("unused")
	private void refreshHandler(Event event) {
		fullRefresh();
	}
	
	
	/**
	 * 	Internal handler for sort events
	 * 
	 * 	@param event sort event
	 */
//	private function sortChangedHandler(event:Event):void {
//		if (this.sort != null)
//			this.sort.compareFunction = nullCompare;	// Force compare function to do nothing as we use server-side sorting
//	    _fullRefresh = true;
//	}
	
    /**
		 * Internal handler for sort field descending events
	 * 
	 * @param event descendingChange event
	 */
//	private function sortFieldChangeHandler(event:Event):void {
//		_fullRefresh = true;
//	}
			
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
	    
	    handleResult(result, event);
	}
	
	/**
	 * 	@private
	 *	Event handler for results query
	 * 
	 *  @param result the result object
	 *  @param event the result event
	 */
	@SuppressWarnings("unchecked")
	protected void handleResult(Map<String, Object> result, TideResultEvent<?> event) {
		list = (List<T>)result.get("resultList");
		
//		if (this.sort != null)
//			this.sort.compareFunction = nullCompare;	// Avoid automatic sorting
		
		int expectedFirst = 0;
		int expectedLast = 0;
		
//		var dispatchReset:Boolean = _initializing && this.dispatchResetOnInit;
		if (initializing && event != null) {
			if (max == 0 && result.containsKey("maxResults"))
		    	max = (Integer)result.get("maxResults");
		    initialize(event);
		}
		
		int nextFirst = (Integer)result.get("firstResult");
		int nextLast = nextFirst + (Integer)result.get("maxResults");
		log.debug("handle result %d - %d", nextFirst, nextLast);
		
		if (!initializing) {
		    expectedFirst = nextFirst;
		    expectedLast = nextLast;
		}
		@SuppressWarnings("unused")
		int page = nextFirst / max;
		// log.debug("findResult page {0} ({1} - {2})", page, nextFirst, nextLast);
		
		@SuppressWarnings("unused")
		int newCount = (Integer)result.get("resultCount");
//		if (newCount != count) {
//		    var pce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, "length", _count, newCount); 
//			_count = newCount;
//			dispatchEvent(pce);
//		}
	
	    initializing = false;
		
        boolean dispatchRefresh = (localIndex == null);
	    
	    if (localIndex != null) {
	    	List<String> entityNames = new ArrayList<String>();
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName) && !entityNames.contains(entityName))
					entityNames.add(entityName);

	            stopTrackUpdates(localIndex[i]);
	        }
	     // TODO
//	        for (String entityName : entityNames)
//	        	context.removeEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler);
	    }
	    for (Object o : list) {
	    	if (elementClass == null || (o != null && o.getClass().isAssignableFrom(elementClass)))
	    		elementClass = (Class<? extends T>)o.getClass();
	    }
	    localIndex = (T[])Array.newInstance(elementClass, list.size());
		localIndex = list.toArray(localIndex);
	    if (localIndex != null) {
	    	List<String> entityNames = new ArrayList<String>();
	        for (int i = 0; i < localIndex.length; i++) {
				String entityName = localIndex[i].getClass().getSimpleName();
				if (!entityName.equals(elementName) && !entityNames.contains(entityName))
					entityNames.add(entityName);
					
	            startTrackUpdates(localIndex[i]);
	        }
// TODO
//	        for (String entityName : entityNames)
//	        	context.addEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler, false, 0, true);
	    }
	    
		// Must be before collection event dispatch because it can trigger a new getItemAt
		this.first = nextFirst;
		this.last = nextLast;
	    
	    if (ipes != null) {
	        List<Object[]> nextIpes = new ArrayList<Object[]>();
	        
		    while (!ipes.isEmpty()) {
		        // Must pop the ipe before calling result
		        Object[] a0 = ipes.remove(ipes.size()-1);
		        if ((Integer)a0[1] == expectedFirst && (Integer)a0[2] == expectedLast) {
		        	ItemPendingException ipe = (ItemPendingException)a0[0];
		        	ipe.callRespondersResult(event);
		        }
		        else
		            nextIpes.add(a0);
		    }
		    
		    ipes = nextIpes;
		}
		
//	    _ipes = null;
	    
//	    if (event != null)
//	    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, RESULT, -1, -1, [ event ]));
	    
	    if (dispatchRefresh) {
//	        _tempSort = new NullSort();
//        	saveThrowIpe = _throwIpe;
//            _throwIpe = false;
	        refresh();
//	        _throwIpe = saveThrowIpe;
//	        _tempSort = null;
	    }
	    
	    maxGetAfterHandle = -1;
	    firstGetNext = -1;
	    
//	    if (dispatchReset)
//	    	dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, CollectionEventKind.RESET));
	}
	
	
//	public override function dispatchEvent(event:Event):Boolean {
//		if (_tempSort is NullSort && event is CollectionEvent && CollectionEvent(event).kind == CollectionEventKind.REFRESH)
//			CollectionEvent(event).kind = CollectionEventKind.RESET;
//		return super.dispatchEvent(event);
//	} 
	
	
//	private var _tempSort:NullSort = null;
	
	// All this ugly hack because ListCollectionView.revision is private
//    mx_internal override function getBookmarkIndex(bookmark:CursorBookmark):int {
//    	var saveThrowIpe:Boolean = _throwIpe;
//        _throwIpe = false;
//        var index:int = super.getBookmarkIndex(bookmark);
//        _throwIpe = saveThrowIpe;
//        return index;
//    }


//    [Bindable("listChanged")]
//	public override function get list():IList {
//	    return _list;
//	}
//
//    CONFIG::flex40 {
//        [Bindable("sortChanged")]
//        public override function get sort():Sort {
//            if (_tempSort && !_tempSort.sorted)
//                return _tempSort;
//            return super.sort;
//        }
//
//        public override function set sort(newSort:Sort):void {
//            // Track changes on sort fields
//            if (sort != null && sort.fields != null) {
//                for each (var field:SortField in sort.fields)
//                    field.removeEventListener("descendingChanged", sortFieldChangeHandler);
//            }
//            _sortFieldListenersSet = 0;
//            super.sort = newSort;
//        }
//    }
//
//    CONFIG::flex45 {
//        [Bindable("sortChanged")]
//        public override function get sort():ISort {
//            if (_tempSort && !_tempSort.sorted)
//                return _tempSort;
//            return super.sort;
//        }
//
//        public override function set sort(newSort:ISort):void {
//            // Track changes on sort fields
//            if (sort != null && sort.fields != null) {
//                for each (var field:SortField in sort.fields)
//                    field.removeEventListener("descendingChanged", sortFieldChangeHandler);
//            }
//            _sortFieldListenersSet = 0;
//            super.sort = newSort;
//        }
//    }
	
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
		
		List<Object[]> nextIpes = new ArrayList<Object[]>();
		if (ipes != null) {
			while (ipes.size() > 0) {
				Object[] a = (Object[])ipes.remove(ipes.size()-1);
				ItemPendingException ipe = (ItemPendingException)a[0];
				
				if ((max == 0 && (Integer)a[1] == 0 && (Integer)a[2] == 0) || ((Integer)a[1] == first && (Integer)a[2] == last))
					ipe.callRespondersFault(event);
				else
					nextIpes.add(a);
			}
			ipes = nextIpes;
		}
	    
//    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, FAULT, -1, -1, [ event ]));
	    
	    maxGetAfterHandle = -1;
	    firstGetNext = -1;
	}
	
	
	private int maxGetAfterHandle = -1;
	private int firstGetNext = -1;
	
	
	/**
	 * 	Override of getItemAt with ItemPendingError management
	 * 
	 *	@param index index of requested item
	 *	@param prefetch not used
	 *  @return object at specified index
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		if (index < 0)
			return null;
	
		// log.debug("get item at {0}", index);
		
		if (max == 0 || initializing) {
			if (!initSent) {
				log.debug("initial find");
			    find(0, max);
			    initSent = true;
			}
		    return null;
		}

		if (firstGetNext == -1) {
			if (maxGetAfterHandle == -1)
				maxGetAfterHandle = index;
			else if (index > maxGetAfterHandle)
				maxGetAfterHandle = index;
				
			if (index < maxGetAfterHandle)
				firstGetNext = index;
		}
		else if (index > maxGetAfterHandle && firstGetNext < maxGetAfterHandle)
			firstGetNext = index;
	
		if (localIndex != null && ipes != null) {
			// Check if requested page is already pending, and rethrow existing error
			// Always rethrow when data is after (workaround for probable bug of Flex DataGrid)
			for (int i = 0; i < ipes.size(); i++) {
				Object[] a = (Object[])ipes.get(i);
				ItemPendingException ipe = (ItemPendingException)a[0];
				if (index >= (Integer)a[1] && index < (Integer)a[2] && (Integer)a[2] > last && !ipe.hasResponders() && index != firstGetNext) {
				    log.debug("forced rethrow of existing IPE for index %d (%d - %d)", index, a[1], a[2]);
					((List<Integer>)a[3]).add(index);
				    // log.debug("stacktrace {0}", ipe.getStackTrace());
				    throw ipe;
				}
			}
		}
	    
		if (localIndex != null && index >= first && index < last) {	// Local data available for index
		    int j = index-first;
		    // log.debug("getItemAt index {0} (current {1} to {2})", index, _first, _last);
			return localIndex[j];
		}
		
		if (ipes != null) {
			// Check if requested page is already pending, and rethrow existing error
			for (int i = 0; i < ipes.size(); i++) {
				Object[] a = (Object[])ipes.get(i);
				ItemPendingException ipe = (ItemPendingException)a[0];
				if (index >= (Integer)a[1] && index < (Integer)a[2]) {
				    log.debug("rethrow existing IPE for index %d (%d - %d)", index, a[1], a[2]);
					((List<Integer>)a[3]).add(index);
					throw ipe;
				}
			}
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
		
		// Throw ItemPendingError for requested index
		// log.debug("ItemPendingError for index " + index + " triggered " + nfi + " to " + nla);
		ItemPendingException ipe = new ItemPendingException("Items pending from " + nfi + " to " + nla + " for index " + index);
		if (ipes == null)
			ipes = new ArrayList<Object[]>();
		List<Integer> indices = new ArrayList<Integer>();
		indices.add(index);
		ipes.add(new Object[] { ipe, nfi, nla, indices });
	    log.debug("throw IPE for index %d (%d - %d)", index, nfi, nla);
	    // log.debug("stacktrace {0}", ipe.getStackTrace());
		throw ipe;
	}
	
	
    protected void startTrackUpdates(T item) {
//        if (item != null)
//            IEventDispatcher(item).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler, false, 0, true);
    }
    
    protected void stopTrackUpdates(T item) {
//        if (item != null)
//            IEventDispatcher(item).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, itemUpdateHandler);    
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

}
