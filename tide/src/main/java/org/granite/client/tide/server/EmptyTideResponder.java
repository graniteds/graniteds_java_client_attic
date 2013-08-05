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

package org.granite.client.tide.server;

/**
 * @author William DRAI
 */
public class EmptyTideResponder<T> implements TideMergeResponder<T> {
	
	private final T mergeWith;
	
	private EmptyTideResponder(T mergeWith) {
		this.mergeWith = mergeWith;
	}

	@Override
	public final void result(TideResultEvent<T> event) {
		// Do nothing
	}
	
	@Override
	public final void fault(TideFaultEvent event) {
		// Do nothing
	}
	
	@Override
	public T getMergeResultWith() {
		return mergeWith;
	}
	
	public static <T> TideMergeResponder<T> mergeWith(T mergeWith) {
		return new EmptyTideResponder<T>(mergeWith);
	}

}
