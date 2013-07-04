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

package org.granite.client.persistence.collection.javafx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.collections.FXCollections;

import org.apache.http.MethodNotSupportedException;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.PersistentSortedMap;
import org.granite.client.persistence.collection.PersistentSortedSet;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentCollections {

	private ObservablePersistentCollections() {
	}
	
	public static <E> ReadOnlyListWrapper<E> newReadOnlyPersistentListWrapper(Object bean, String name) {
		return new ReadOnlyListWrapper<E>(bean, name, newObservablePersistentList(new PersistentList<E>(true)));
	}
	
	public static <E> ReadOnlyListWrapper<E> newReadOnlyPersistentListWrapper(Object bean, String name, PersistentList<E> persistentList) {
		return new ReadOnlyListWrapper<E>(bean, name, newObservablePersistentList(persistentList));
	}
	
	public static <E> ObservablePersistentList<E> newObservablePersistentList() {
		return newObservablePersistentList(new PersistentList<E>(true));
	}

	@SuppressWarnings("unchecked")
	public static <E> ObservablePersistentList<E> newObservablePersistentList(PersistentList<E> persistentList) {
		return (ObservablePersistentList<E>)Proxy.newProxyInstance(
			persistentList.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentList.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableList(persistentList), persistentList)
		);
	}
	
	public static <E> ReadOnlySetWrapper<E> newReadOnlyPersistentSetWrapper(Object bean, String name) {
		return new ReadOnlySetWrapper<E>(bean, name, newObservablePersistentSet(new PersistentSet<E>(true)));
	}
	
	public static <E> ReadOnlySetWrapper<E> newReadOnlyPersistentSetWrapper(Object bean, String name, PersistentSet<E> persistentSet) {
		return new ReadOnlySetWrapper<E>(bean, name, newObservablePersistentSet(persistentSet));
	}
		
	public static <E> ObservablePersistentSet<E> newObservablePersistentSet() {
		return newObservablePersistentSet(new PersistentSet<E>(true));
	}

	@SuppressWarnings("unchecked")
	public static <E> ObservablePersistentSet<E> newObservablePersistentSet(PersistentSet<E> persistentSet) {
		return (ObservablePersistentSet<E>)Proxy.newProxyInstance(
			persistentSet.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentSet.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableSet(persistentSet), persistentSet)
		);
	}
	
	public static <E> ReadOnlySetWrapper<E> newReadOnlyPersistentSortedSetWrapper(Object bean, String name) {
		return new ReadOnlySetWrapper<E>(bean, name, newObservablePersistentSortedSet(new PersistentSortedSet<E>(true)));
	}
	
	public static <E> ReadOnlySetWrapper<E> newReadOnlyPersistentSortedSetWrapper(Object bean, String name, PersistentSortedSet<E> persistentSortedSet) {
		return new ReadOnlySetWrapper<E>(bean, name, newObservablePersistentSortedSet(persistentSortedSet));
	}
		
	public static <E> ObservablePersistentSortedSet<E> newObservablePersistentSortedSet() {
		return newObservablePersistentSortedSet(new PersistentSortedSet<E>(true));
	}

	@SuppressWarnings("unchecked")
	public static <E> ObservablePersistentSortedSet<E> newObservablePersistentSortedSet(PersistentSortedSet<E> persistentSortedSet) {
		return (ObservablePersistentSortedSet<E>)Proxy.newProxyInstance(
			persistentSortedSet.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentSortedSet.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableSet(persistentSortedSet), persistentSortedSet)
		);
	}
	
	public static <E> ReadOnlyListWrapper<E> newReadOnlyPersistentBagWrapper(Object bean, String name) {
		return new ReadOnlyListWrapper<E>(bean, name, newObservablePersistentBag(new PersistentBag<E>(true)));
	}
	
	public static <E> ReadOnlyListWrapper<E> newReadOnlyPersistentBagWrapper(Object bean, String name, PersistentBag<E> persistentBag) {
		return new ReadOnlyListWrapper<E>(bean, name, newObservablePersistentBag(persistentBag));
	}
	
	public static <E> ObservablePersistentBag<E> newObservablePersistentBag() {
		return newObservablePersistentBag(new PersistentBag<E>(true));
	}
	
	@SuppressWarnings("unchecked")
	public static <E> ObservablePersistentBag<E> newObservablePersistentBag(PersistentBag<E> persistentBag) {
		return (ObservablePersistentBag<E>)Proxy.newProxyInstance(
			persistentBag.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentBag.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableList(persistentBag), persistentBag)
		);
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> newReadOnlyPersistentMapWrapper(Object bean, String name) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, newObservablePersistentMap(new PersistentMap<K, V>(true)));
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> newReadOnlyPersistentMapWrapper(Object bean, String name, PersistentMap<K, V> persistentMap) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, newObservablePersistentMap(persistentMap));
	}
	
	public static <K, V> ObservablePersistentMap<K, V> newObservablePersistentMap() {
		return newObservablePersistentMap(new PersistentMap<K, V>(true));
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> ObservablePersistentMap<K, V> newObservablePersistentMap(PersistentMap<K, V> persistentMap) {
		return (ObservablePersistentMap<K, V>)Proxy.newProxyInstance(
			persistentMap.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentMap.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableMap(persistentMap), persistentMap)
		);
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> newReadOnlyPersistentSortedMapWrapper(Object bean, String name) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, newObservablePersistentSortedMap(new PersistentSortedMap<K, V>(true)));
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> newReadOnlyPersistentSortedMapWrapper(Object bean, String name, PersistentSortedMap<K, V> persistentSortedMap) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, newObservablePersistentSortedMap(persistentSortedMap));
	}
	
	public static <K, V> ObservablePersistentSortedMap<K, V> newObservablePersistentSortedMap() {
		return newObservablePersistentSortedMap(new PersistentSortedMap<K, V>(true));
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> ObservablePersistentSortedMap<K, V> newObservablePersistentSortedMap(PersistentSortedMap<K, V> persistentSortedMap) {
		return (ObservablePersistentSortedMap<K, V>)Proxy.newProxyInstance(
			persistentSortedMap.getClass().getClassLoader(),
			new Class<?>[]{ ObservablePersistentSortedMap.class },
			new ObservablePersistentCollectionHandler(FXCollections.observableMap(persistentSortedMap), persistentSortedMap)
		);
	}
	
	private static class ObservablePersistentCollectionHandler implements InvocationHandler, Externalizable {

		private final Observable observable;
		private final PersistentCollection persistentCollection;
		
		protected ObservablePersistentCollectionHandler(Observable observable, PersistentCollection persistentCollection) {
			this.observable = observable;
			this.persistentCollection = persistentCollection;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?> declaringClass = method.getDeclaringClass();
			
			if (declaringClass == ObservablePersistentCollection.class) {
				if ("internalPersistentCollection".equals(method.getName()))
					return persistentCollection;
				throw new MethodNotSupportedException(method.toString());
			}
			
			Object instance = declaringClass.isInstance(observable) ? observable : persistentCollection;
			try {
				return method.invoke(instance, args);
			}
			catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			persistentCollection.readExternal(in);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			persistentCollection.writeExternal(out);
		}
	}
}
