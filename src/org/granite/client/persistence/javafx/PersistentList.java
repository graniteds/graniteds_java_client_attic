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

package org.granite.client.persistence.javafx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.client.persistence.LazyableCollection;
import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;

/**
 * @author William DRAI
 */
@RemoteClass("org.granite.messaging.persistence.ExternalizablePersistentList")
public class PersistentList<T> implements ObservableList<T>, LazyableCollection, Externalizable {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistentList.class);

    @SuppressWarnings("unused")
	private boolean initializing = false;
    private boolean initialized = false;
    private String metadata = null;
    private boolean dirty = false;
    
    private ObservableList<T> olist;
   
    private ListChangeListener<T> listener = new ListChangeListener<T>() {
        public void onChanged(ListChangeListener.Change<? extends T> change) {
            if (!initialized)
                return;
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasReplaced() || change.wasPermutated()) {
                    dirty = true;
                    break;
                }
            }
        }
    };

    
    public PersistentList() {
        this.olist = FXCollections.observableArrayList();
        this.initialized = true;
        addListener(listener);
    }

    public PersistentList(Set<T> set) {
        this.olist = FXCollections.observableArrayList(set);
        this.initialized = true;
        addListener(listener);
    }
    
    public PersistentList(boolean initialized) {
        this.olist = FXCollections.observableArrayList();
        this.initialized = initialized;         
        if (initialized)
            addListener(listener);
    }


    public final boolean isInitialized() {
        return initialized;
    }

    public void initializing() {
        clear();
        initializing = true;
        dirty = false;
        removeListener(listener);
    }

    public void initialize() {
        initializing = false;
        initialized = true;
        dirty = false;
        addListener(listener);
    }

    public void uninitialize() {
        removeListener(listener);
        initialized = false;
        clear();
        dirty = false;
    }
    
    public PersistentList<T> clone(boolean uninitialize) {
        PersistentList<T> coll = new PersistentList<T>(initialized && !uninitialize);
        coll.metadata = metadata;
        if (initialized) {
            for (T obj : this)
                coll.add(obj);
        }
        coll.dirty = dirty;
        return coll; 
    }
    
    public void addListener(InvalidationListener listener) {
        olist.addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
        olist.removeListener(listener);
    }
    
    private Map<ListChangeListener<? super T>, ListChangeListener<? super T>> listenerWrappers = new IdentityHashMap<ListChangeListener<? super T>, ListChangeListener<? super T>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = new ListChangeListenerWrapper(this, listener);
        listenerWrappers.put(listener, listenerWrapper);
        olist.addListener(listenerWrapper);
    }

	public void removeListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = listenerWrappers.remove(listener);
        if (listenerWrapper != null)
        	olist.removeListener(listenerWrapper);
    }
    

    public boolean isEmpty() {
        return olist.isEmpty();
    }

    public boolean contains(Object o) {
        return olist.contains(o);
    }

    public Iterator<T> iterator() {
        return olist.iterator();
    }

    public boolean add(T e) {
        return olist.add(e);
    }

    public boolean remove(Object o) {
        return olist.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return olist.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return olist.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return olist.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return olist.removeAll(c);
    }

    public boolean equals(Object o) {
        return olist.equals(o);
    }

    public int hashCode() {
        return olist.hashCode();
    }

    public T get(int index) {
        return olist.get(index);
    }

    public void add(int index, T element) {
        olist.add(index, element);
    }

    public boolean addAll(T... elements) {
        return olist.addAll(elements);
    }

    public void clear() {
        olist.clear();
    }

    public int indexOf(Object o) {
        return olist.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return olist.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return olist.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return olist.listIterator(index);
    }

    public void remove(int arg0, int arg1) {
        olist.remove(arg0, arg1);
    }

    public T remove(int index) {
        return olist.remove(index);
    }

    public boolean removeAll(T... elements) {
        return olist.removeAll(elements);
    }

    public boolean retainAll(Collection<?> c) {
        return olist.retainAll(c);
    }

    public boolean retainAll(T... elements) {
        return olist.retainAll(elements);
    }

    public T set(int index, T element) {
        return olist.set(index, element);
    }

    public boolean setAll(Collection<? extends T> coll) {
        return olist.setAll(coll);
    }

    public boolean setAll(T... arg0) {
        return olist.setAll(arg0);
    }

    public int size() {
        return olist.size();
    }

    public Object[] toArray() {
        return olist.toArray();
    }

    @SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
        return olist.toArray(a);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return olist.subList(fromIndex, toIndex);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + (initialized ? "" : " (uninitialized)") + (dirty ? " (dirty)" : "") + ":" + olist.toString();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        initialized = ((Boolean)input.readObject()).booleanValue();
        metadata = (String)input.readObject();
        if (initialized) {
            dirty = ((Boolean)input.readObject()).booleanValue();
            T[] array = (T[])input.readObject();
            olist = FXCollections.observableArrayList(array);
        }
    }
    
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeObject(Boolean.valueOf(initialized));
        output.writeObject(metadata);
        if (initialized) {
            output.writeObject(Boolean.valueOf(dirty));
            output.writeObject(olist.toArray());
        }
    }
}
