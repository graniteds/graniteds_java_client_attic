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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSet;

import com.sun.javafx.collections.ObservableSetWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentSet<E> extends ObservableSetWrapper<E> implements UnsafePersistentCollection<PersistentSet<E>> {
	
	private final PersistentSet<E> persistentSet;

	public ObservablePersistentSet(PersistentSet<E> persistentSet) {
		super(persistentSet);
		
		this.persistentSet = persistentSet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return (Iterator<E>)super.iterator();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentSet.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentSet.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentSet.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentSet.uninitialize();
	}

	@Override
	public void initialize() {
		persistentSet.initialize();
	}

	@Override
	public void initializing() {
		persistentSet.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentSet.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentSet.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		persistentSet.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentSet.isDirty();
	}

	@Override
	public void dirty() {
		persistentSet.dirty();
	}

	@Override
	public void clearDirty() {
		persistentSet.clearDirty();
	}

	@Override
	public void addListener(InitializationListener listener) {
		persistentSet.addListener(listener);
	}

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentSet.withInitialized(callback);
	}
	
	@Override
	public PersistentSet<E> internalPersistentCollection() {
		return persistentSet;
	}
}