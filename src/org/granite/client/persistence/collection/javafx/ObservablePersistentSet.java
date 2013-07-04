package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableSet;

import org.granite.client.persistence.collection.PersistentSet;

public interface ObservablePersistentSet<E> extends ObservableSet<E>, ObservablePersistentCollection<PersistentSet<E>> {

}
