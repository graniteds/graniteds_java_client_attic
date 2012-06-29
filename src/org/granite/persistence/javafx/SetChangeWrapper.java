package org.granite.persistence.javafx;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class SetChangeWrapper<T> extends SetChangeListener.Change<T> {            
    private final SetChangeListener.Change<? extends T> wrappedChange;
    
    public SetChangeWrapper(ObservableSet<T> set, SetChangeListener.Change<? extends T> wrappedChange) {
        super(set);
        this.wrappedChange = wrappedChange;
    }

	@Override
	public T getElementAdded() {
		return wrappedChange.getElementAdded();
	}

	@Override
	public T getElementRemoved() {
		return wrappedChange.getElementRemoved();
	}

	@Override
	public boolean wasAdded() {
		return wrappedChange.wasAdded();
	}

	@Override
	public boolean wasRemoved() {
		return wrappedChange.wasRemoved();
	}

      
}