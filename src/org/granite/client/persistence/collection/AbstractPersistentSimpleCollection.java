/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.persistence.collection;

import java.util.Collection;
import java.util.Iterator;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshotFactory;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentSimpleCollection<E, C extends Collection<E>> extends AbstractPersistentCollection<C> implements Collection<E> {

	public AbstractPersistentSimpleCollection() {
	}

	public int size() {
		checkInitialized();
		return getCollection().size();
	}

	public boolean isEmpty() {
		checkInitialized();
		return getCollection().isEmpty();
	}

	public boolean contains(Object o) {
		checkInitialized();
		return getCollection().contains(o);
	}

	public Iterator<E> iterator() {
		checkInitialized();
		return new IteratorProxy<E>(this, getCollection().iterator());
	}

	public Object[] toArray() {
		checkInitialized();
		return getCollection().toArray();
	}

	public <T> T[] toArray(T[] a) {
		checkInitialized();
		return getCollection().toArray(a);
	}

	public boolean add(E e) {
		checkInitialized();
		if (getCollection().add(e)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		checkInitialized();
		if (getCollection().remove(o)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		checkInitialized();
		return getCollection().containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		checkInitialized();
		if (getCollection().addAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean removeAll(Collection<?> c) {
		checkInitialized();
		if (getCollection().removeAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		checkInitialized();
		if (getCollection().retainAll(c)) {
			dirty();
			return true;
		}
		return false;
	}

	public void clear() {
		checkInitialized();
		if (!getCollection().isEmpty()) {
			getCollection().clear();
			dirty();
		}
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance();
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot();
		return factory.newPersistentCollectionSnapshot(true, isDirty(), getCollection());
	}
}
