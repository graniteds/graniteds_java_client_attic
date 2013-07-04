package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableList;

import org.granite.client.persistence.collection.PersistentBag;


public interface ObservablePersistentBag<E> extends ObservableList<E>, ObservablePersistentCollection<PersistentBag<E>> {

}
