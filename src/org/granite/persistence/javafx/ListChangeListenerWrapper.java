package org.granite.persistence.javafx;

import org.granite.persistence.LazyableCollection;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;


public class ListChangeListenerWrapper<T> implements ListChangeListener<T> {
    private final ObservableList<T> wrappedList;
    private final ListChangeListener<T> wrappedListener;
    
    public ListChangeListenerWrapper(ObservableList<T> wrappedList, ListChangeListener<T> wrappedListener) {
        this.wrappedList = wrappedList;
        this.wrappedListener = wrappedListener;
    }
    
    @Override
    public void onChanged(ListChangeListener.Change<? extends T> change) {
        if (!((LazyableCollection)wrappedList).isInitialized())
            return;
        ListChangeListener.Change<T> wrappedChange = new ListChangeWrapper<T>(wrappedList, change);
        wrappedListener.onChanged(wrappedChange);
    }        
}