package org.granite.client.persistence.javafx;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import org.granite.client.persistence.LazyableCollection;


public class SetChangeListenerWrapper<T> implements SetChangeListener<T> {
    private final ObservableSet<T> wrappedSet;
    private final SetChangeListener<T> wrappedListener;
    
    public SetChangeListenerWrapper(ObservableSet<T> wrappedSet, SetChangeListener<T> wrappedListener) {
        this.wrappedSet = wrappedSet;
        this.wrappedListener = wrappedListener;
    }
    
    @Override
    public void onChanged(SetChangeListener.Change<? extends T> change) {
        if (!((LazyableCollection)wrappedSet).isInitialized())
            return;
        SetChangeListener.Change<T> wrappedChange = new SetChangeWrapper<T>(wrappedSet, change);
        wrappedListener.onChanged(wrappedChange);
    }        
}