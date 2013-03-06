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

package org.granite.client.util.javafx;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author William DRAI
 */
public class ListenerHelper<L> {

	private final Class<?> listenerInterface;
	private final Method listenerMethod;
	private L[] listeners = null;
	
	public ListenerHelper(Class<?> listenerInterface) {
		this.listenerInterface = listenerInterface;
		Method[] methods = listenerInterface.getMethods();
		if (!listenerInterface.isInterface())
			throw new RuntimeException("Listener class must be an interface");
		if (methods.length != 1)
			throw new RuntimeException("Cannot use ListenerHelper with listener interfaces having more than one method");
		listenerMethod = methods[0];
	}
	
	@SuppressWarnings("unchecked")
	public void addListener(L listener) {
		if (listeners == null) {
			listeners = (L[])Array.newInstance(listenerInterface, 1);
			listeners[0] = listener;
		}
		else {
			for (L l : listeners) {
				if (listener.equals(l))
					return;
			}
			L[] newListeners = Arrays.copyOf(listeners, listeners.length+1);
			newListeners[listeners.length] = listener;
			listeners = newListeners;
		}
	}
	
	public void removeListener(L listener) {
		if (listeners == null)
			return;
		
		int index = -1;
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i].equals(listener)) {
				index = i;
				break;
			}
		}
		if (index < 0)
			return;
		
		if (listeners.length == 1)
			listeners = null;
		else {
			L[] newListeners = Arrays.copyOf(listeners, listeners.length-1);
			if (index < listeners.length-1)
				System.arraycopy(listeners, index+1, newListeners, index, listeners.length-index-1);
			listeners = newListeners;
		}
	}
	
    public void fireEvent(Object... args) {
    	if (listeners != null) {
    		for (L listener : listeners) {
    			try {
    				listenerMethod.invoke(listener, args);
    			}
    			catch (Exception e) {
    				throw new RuntimeException("Could not fire event", e);
    			}
    		}
    	}
    }
    
    public boolean hasListeners() {
        return listeners != null;
    }
	
}
