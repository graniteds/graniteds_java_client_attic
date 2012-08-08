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

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * @author William DRAI
 */
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