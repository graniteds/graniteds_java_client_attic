/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.engine;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class DefaultEngineStatusHandler implements EngineStatusHandler {

	private static final Logger log = Logger.getLogger(DefaultEngineStatusHandler.class);
	
	@Override
	public void handleIO(boolean active) {
		log.debug(active ? "I/O busy" : "I/O idle");
	}
	
	@Override
	public void handleException(EngineException e) {
		log.error(e, "Engine failed");
	}
}
