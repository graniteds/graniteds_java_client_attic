package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableList;

import org.granite.client.persistence.collection.PersistentList;

public interface ObservablePersistentList<E> extends ObservableList<E>, ObservablePersistentCollection<PersistentList<E>> {

}
