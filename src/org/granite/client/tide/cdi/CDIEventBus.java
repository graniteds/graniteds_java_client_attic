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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.granite.client.tide.Context;
import org.granite.client.tide.impl.SimpleEventBus;

/**
 * @author William DRAI
 */
@ApplicationScoped
public class CDIEventBus extends SimpleEventBus {

	@Inject
	private BeanManager beanManager;
	
	@Override
	public void raiseEvent(Context context, String type, Object... args) {
		beanManager.fireEvent(new TideApplicationEvent(context, type, args));
	}
	
	public void onApplicationEvent(@Observes TideApplicationEvent event) {
	    raiseEvent(event);
	}

}
