package org.granite.client.persistence.javafx;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ListChangeWrapper<T> extends ListChangeListener.Change<T> {
	
    private final ListChangeListener.Change<? extends T> wrappedChange;
    
    private static final int[] EMPTY_PERMUTATION = new int[0];
    
    public ListChangeWrapper(ObservableList<T> list, ListChangeListener.Change<? extends T> wrappedChange) {
        super(list);
        this.wrappedChange = wrappedChange;
    }

    @Override
	public int getAddedSize() {
		return wrappedChange.getAddedSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> getAddedSubList() {
		return (List<T>)wrappedChange.getAddedSubList();
	}

	@Override
	public int getRemovedSize() {
		return wrappedChange.getRemovedSize();
	}

	@Override
	public boolean wasAdded() {
		return wrappedChange.wasAdded();
	}

	@Override
	public boolean wasPermutated() {
		return wrappedChange.wasPermutated();
	}

	@Override
	public boolean wasRemoved() {
		return wrappedChange.wasRemoved();
	}

	@Override
	public boolean wasReplaced() {
		return wrappedChange.wasReplaced();
	}

	@Override
	public boolean wasUpdated() {
		return wrappedChange.wasUpdated();
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
        return EMPTY_PERMUTATION;
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