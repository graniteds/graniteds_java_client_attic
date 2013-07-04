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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentCollection;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public class JavaFXManagedPersistentCollection<T> extends AbstractJavaFXManagedPersistentAssociation implements ObservableList<T>, ManagedPersistentCollection<T> {
    
    private final ObservableList<T> list;
    
    
    @SuppressWarnings("unchecked")
	public JavaFXManagedPersistentCollection(Object entity, String propertyName, LazyableCollection list) {
        super(entity, propertyName);
        this.list = (ObservableList<T>)list;
    }
    
    
    public Object getObject() {
        return list;
    }

    @Override
    public LazyableCollection getCollection() {
        return (LazyableCollection)list;
    }
   
    public boolean isInitialized() {
        return ((LazyableCollection)list).isInitialized();
    }    
    
    public int size() {
        if (checkForRead())
            return list.size();
        return 0;
    }

    public boolean isEmpty() {
        if (checkForRead())
            return list.isEmpty();
        return true;
    }

    @Override
    public void clear() {
        boolean wasInitialized = checkForRead(false);
        list.clear();
        if (!wasInitialized)
            initialize();
    }
    
    public T get(int index) {
        if (checkForRead())
            return list.get(index);
        return null;
    }

    @Override
    public boolean contains(Object item) {
        if (checkForRead())
            return list.contains(item);
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> items) {
        if (checkForRead())
            return list.containsAll(items);
        return false;
    }

    @Override
    public Object[] toArray() {
        if (checkForRead())
            return list.toArray();
        return null;
    }

    @SuppressWarnings("hiding")
	@Override
    public <T> T[] toArray(T[] a) {
        if (checkForRead())
            return list.toArray(a);
        return null;
    }
    
    public boolean add(T item) {
        checkForWrite();
        return list.add(item);
    }
    
    public void add(int index, T item) {
        checkForWrite();
        list.add(index, item);
    }
    
    public boolean addAll(Collection<? extends T> items) {
        checkForWrite();
        return list.addAll(items);
    }
    
    public boolean addAll(int index, Collection<? extends T> items) {
        checkForWrite();
        return list.addAll(index, items);
    }
    
    public boolean addAll(T... items) {
        checkForWrite();
        return list.addAll(items);
    }
    
    public T set(int index, T item) {
        checkForWrite();
        return list.set(index, item);
    }
    
    public boolean setAll(Collection<? extends T> items) {
        checkForWrite();
        return list.setAll(items);
    }
    
    public boolean setAll(T... items) {
        checkForWrite();
        return list.setAll(items);
    }
    
    public T remove(int index) {
        checkForWrite();
        return list.remove(index);
    }
    
    public boolean remove(Object item) {
        checkForWrite();
        return list.remove(item);
    }
    
    public void remove(int fromIndex, int toIndex) {
        checkForWrite();
        list.remove(fromIndex, toIndex);
    }
    
    public boolean removeAll(Collection<?> items) {
        checkForWrite();
        return list.removeAll(items);
    }
    
    public boolean removeAll(T... items) {
        checkForWrite();
        return list.removeAll(items);
    }

    @Override
    public boolean retainAll(Collection<?> items) {
        checkForWrite();
        return list.retainAll(items);
    }

    @Override
    public boolean retainAll(T... items) {
        checkForWrite();
        return list.retainAll(items);
    }

    @Override
    public int indexOf(Object item) {
        if (checkForRead())
            return list.indexOf(item);
        return -1;
    }

    @Override
    public int lastIndexOf(Object item) {
        if (checkForRead())
            return list.lastIndexOf(item);
        return -1;
    }

    @Override
    public Iterator<T> iterator() {
    	return new CheckReadIterator(0);
    }

    @Override
    public ListIterator<T> listIterator() {
    	return new CheckReadIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
    	return new CheckReadIterator(index);
    }
    
    public class CheckReadIterator implements ListIterator<T> {
    	
    	private final ListIterator<T> listIterator;
    	
    	public CheckReadIterator(int index) {
    		if (checkForRead())
    			listIterator = list.listIterator(index);
    		else
    			listIterator = null;
    	}

		@Override
		public boolean hasNext() {
			return listIterator != null ? listIterator.hasNext() : false;
		}

		@Override
		public T next() {
			if (listIterator != null)
				return listIterator.next();
			throw new IllegalStateException("Cannot call next() on uninitialized collection");
		}

		@Override
		public boolean hasPrevious() {
			return listIterator != null ? listIterator.hasNext() : false;
		}

		@Override
		public T previous() {
			if (listIterator != null)
				return listIterator.previous();
			throw new IllegalStateException("Cannot call previous() on iterator for uninitialized collection");
		}

		@Override
		public int nextIndex() {
			return listIterator != null ? listIterator.nextIndex() : 0;
		}

		@Override
		public int previousIndex() {
			return listIterator != null ? listIterator.previousIndex() : 0;
		}

		@Override
		public void remove() {
			if (listIterator != null)
				listIterator.remove();
			else
				throw new IllegalStateException("Cannot call remove() on iterator for uninitialized collection");
		}

		@Override
		public void set(T e) {
			if (listIterator != null)
				listIterator.set(e);
			else
				throw new IllegalStateException("Cannot call set() on iterator for uninitialized collection");
		}

		@Override
		public void add(T e) {
			if (listIterator != null)
				listIterator.add(e);
			else
				throw new IllegalStateException("Cannot call add() on iterator for uninitialized collection");
		}
    	
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (checkForRead())
            return list.subList(fromIndex, toIndex);
        return null;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        list.addListener(listener);
    }

    @Override
    public void addListener(ListChangeListener<? super T> listener) {
        list.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        list.removeListener(listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super T> listener) {
        list.removeListener(listener);
    }

    @Override
    public LazyableCollection clone(boolean uninitialize) {
    	JavaFXManagedPersistentCollection<T> coll = new JavaFXManagedPersistentCollection<T>(getOwner(), getPropertyName(), ((LazyableCollection)list).clone(uninitialize));
    	coll.setServerSession(getServerSession());
    	return coll;
    }
}