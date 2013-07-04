package org.granite.client.persistence.collection.javafx;

import javafx.beans.Observable;

import org.granite.client.persistence.collection.PersistentCollection;

public interface ObservablePersistentCollection<C extends PersistentCollection> extends Observable, PersistentCollection {

	C internalPersistentCollection();
}
