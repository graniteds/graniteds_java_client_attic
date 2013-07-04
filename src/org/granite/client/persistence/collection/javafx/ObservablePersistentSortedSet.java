package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableSet;

import org.granite.client.persistence.collection.PersistentSortedSet;

public interface ObservablePersistentSortedSet<E> extends ObservableSet<E>, ObservablePersistentCollection<PersistentSortedSet<E>> {

}
