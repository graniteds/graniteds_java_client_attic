package org.granite.client.util.javafx;

import java.util.Arrays;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;


public class ListListenerHelper<E> {

	private InvalidationListener[] invalidationListeners = null;
	private ListChangeListener<? super E>[] listChangeListeners = null;
	
	public void addListener(InvalidationListener listener) {
		if (invalidationListeners == null)
			invalidationListeners = new InvalidationListener[] { listener };
		else {
			for (InvalidationListener invalidationListener : invalidationListeners) {
				if (invalidationListener.equals(listener))
					return;
			}
			InvalidationListener[] newInvalidationListeners = Arrays.copyOf(invalidationListeners, invalidationListeners.length+1);
			newInvalidationListeners[invalidationListeners.length] = listener;
			invalidationListeners = newInvalidationListeners;
		}
	}
	
	public void removeListener(InvalidationListener listener) {
		if (invalidationListeners == null)
			return;
		
		int index = -1;
		for (int i = 0; i < invalidationListeners.length; i++) {
			if (invalidationListeners[i].equals(listener)) {
				index = i;
				break;
			}
		}
		if (index < 0)
			return;
		
		if (invalidationListeners.length == 1)
			invalidationListeners = null;
		else {
			InvalidationListener[] newInvalidationListeners = Arrays.copyOf(invalidationListeners, invalidationListeners.length-1);
			if (index < invalidationListeners.length-1)
				System.arraycopy(invalidationListeners, index+1, newInvalidationListeners, index, invalidationListeners.length-index-1);
			invalidationListeners = newInvalidationListeners;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addListener(ListChangeListener<? super E> listener) {
		if (listChangeListeners == null)
			listChangeListeners = new ListChangeListener[] { listener };
		else {
			ListChangeListener<? super E>[] newListChangeListeners = Arrays.copyOf(listChangeListeners, listChangeListeners.length+1);
			newListChangeListeners[listChangeListeners.length] = listener;
			listChangeListeners = newListChangeListeners;
		}
	}
	
	public void removeListener(ListChangeListener<? super E> listener) {
		if (listChangeListeners == null)
			return;
		
		int index = -1;
		for (int i = 0; i < listChangeListeners.length; i++) {
			if (listChangeListeners[i].equals(listener)) {
				index = i;
				break;
			}
		}
		if (index < 0)
			return;
		
		if (listChangeListeners.length == 1)
			listChangeListeners = null;
		else {
			ListChangeListener<? super E>[] newListChangeListeners = Arrays.copyOf(listChangeListeners, listChangeListeners.length-1);
			if (index < listChangeListeners.length-1)
				System.arraycopy(listChangeListeners, index+1, newListChangeListeners, index, listChangeListeners.length-index-1);
			listChangeListeners = newListChangeListeners;
		}
	}
	
    public void fireValueChangedEvent(ListChangeListener.Change<E> change) {
    	if (invalidationListeners != null) {
    		for (InvalidationListener invalidationListener : invalidationListeners)
    			invalidationListener.invalidated(change.getList());
    	}
        if (listChangeListeners != null) {
        	for (ListChangeListener<? super E> listChangeListener : listChangeListeners) {
        		change.reset();
        		listChangeListener.onChanged(change);
        	}
        }
    }

    public boolean hasListeners() {
        return invalidationListeners != null || listChangeListeners != null;
    }
	
}
