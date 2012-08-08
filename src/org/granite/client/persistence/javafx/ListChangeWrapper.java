/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.persistence.javafx;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * @author William DRAI
 */
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