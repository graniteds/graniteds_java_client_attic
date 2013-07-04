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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class PersistentSet<T> extends org.granite.client.persistence.collection.PersistentList<T> implements ObservableList<T>, Externalizable {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistentSet.class);

    
    public PersistentSet() {
    	this(true);
    }
    
    public PersistentSet(Set<T> set) {
    	super(FXCollections.observableArrayList(set));
    }
    
    public PersistentSet(boolean initialized) {
    	super();
    	if (initialized)
    		init(FXCollections.observableList(new ArrayList<T>()), false);
    }
    
    @Override
    public ObservableList<T> getCollection() {
    	return (ObservableList<T>)super.getCollection();
    }
    
    public void addListener(InvalidationListener listener) {
        getCollection().addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
    	getCollection().removeListener(listener);
    }
    
    private Map<ListChangeListener<? super T>, ListChangeListener<? super T>> listenerWrappers 
    	= new IdentityHashMap<ListChangeListener<? super T>, ListChangeListener<? super T>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = new ListChangeListenerWrapper(this, listener);
        listenerWrappers.put(listener, listenerWrapper);
        getCollection().addListener(listenerWrapper);
    }

    public void removeListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = listenerWrappers.remove(listener);
        if (listenerWrapper != null)
        	getCollection().removeListener(listenerWrapper);
    }
    
    public boolean addAll(Collection<? extends T> c) {
        List<T> toAdd = new ArrayList<T>(c);
        toAdd.removeAll(getCollection());
        return getCollection().addAll(toAdd);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        List<T> toAdd = new ArrayList<T>(c);
        toAdd.removeAll(getCollection());
        return getCollection().addAll(index, toAdd);
    }

    public boolean addAll(T... elements) {
        List<T> toAdd = Arrays.asList(elements);
        toAdd.removeAll(getCollection());
        return getCollection().addAll(toAdd);
    }

    public void remove(int first, int last) {
    	getCollection().remove(first, last);
    }

    public boolean removeAll(T... elements) {
        return getCollection().removeAll(elements);
    }

    public boolean retainAll(T... elements) {
        return getCollection().retainAll(elements);
    }

    public boolean setAll(Collection<? extends T> coll) {
        return getCollection().setAll(coll);
    }

    public boolean setAll(T... arg0) {
        return getCollection().setAll(arg0);
    }
}
