package org.granite.client.persistence.collection.javafx;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentList;

import com.sun.javafx.collections.ObservableListWrapper;


public class ObservablePersistentListWrapper<E> extends ObservableListWrapper<E> implements ObservablePersistentCollection<PersistentList<E>> {
	
	private final PersistentList<E> wrappedList;

	public ObservablePersistentListWrapper(PersistentList<E> list) {
		super(list);
		this.wrappedList = list;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		wrappedList.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		wrappedList.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return wrappedList.wasInitialized();
	}

	@Override
	public void uninitialize() {
		wrappedList.uninitialize();
	}

	@Override
	public void initialize() {
		wrappedList.initialize();
	}

	@Override
	public void initializing() {
		wrappedList.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return wrappedList.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return wrappedList.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		wrappedList.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return wrappedList.isDirty();
	}

	@Override
	public void dirty() {
		wrappedList.dirty();
	}

	@Override
	public void clearDirty() {
		wrappedList.clearDirty();
	}

	@Override
	public void addListener(InitializationListener listener) {
		wrappedList.addListener(listener);
	}

	@Override
	public void withInitialized(InitializationCallback callback) {
		wrappedList.withInitialized(callback);
	}
	
	@Override
	public PersistentList<E> internalPersistentCollection() {
		return wrappedList;
	}
}