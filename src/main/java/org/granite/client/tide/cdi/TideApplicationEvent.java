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

package org.granite.client.tide.cdi;

import org.granite.client.tide.Context;
import org.granite.client.tide.events.TideEvent;

/**
 * @author William DRAI
 */
public class TideApplicationEvent implements TideEvent {
	
	private final Context context;
	private final String type;
	private final Object[] args;

	
	public TideApplicationEvent(Context context, String type, Object... args) {
		this.context = context;
		this.type = type;
		this.args = args;
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getType() {
		return type;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
}
