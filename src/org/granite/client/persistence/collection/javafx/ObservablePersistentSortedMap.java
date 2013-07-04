package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableMap;

import org.granite.client.persistence.collection.PersistentSortedMap;

public interface ObservablePersistentSortedMap<K, V> extends ObservableMap<K, V>, ObservablePersistentCollection<PersistentSortedMap<K, V>> {

}
