package org.granite.tide.javafx;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ListChangeWrapper<T> extends ListChangeListener.Change<T> {            
    private final ListChangeListener.Change<? extends T> wrappedChange;
    
    public ListChangeWrapper(ObservableList<T> list, ListChangeListener.Change<? extends T> wrappedChange) {
        super(list);
        this.wrappedChange = wrappedChange;
    }

    @Override
    public int getFrom() {
        return wrappedChange.getFrom();
    }

    @Override
    public int getTo() {
        return wrappedChange.getTo();
    }

    @Override
    protected int[] getPermutation() {
        // TODO
        return new int[0]; // wrappedChange.getPermutation();
    }

    @Override
    public int getPermutation(int num) {
        return wrappedChange.getPermutation(num);
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<T> getRemoved() {
        return (List<T>)wrappedChange.getRemoved();
    }

    @Override
    public boolean next() {
        return wrappedChange.next();
    }

    @Override
    public void reset() {
        wrappedChange.reset();
    }        
}