package org.granite.client.persistence.collection.javafx;

import javafx.collections.ObservableMap;

import org.granite.client.persistence.collection.PersistentMap;

public interface ObservablePersistentMap<K, V> extends ObservableMap<K, V>, ObservablePersistentCollection<PersistentMap<K, V>> {

}
