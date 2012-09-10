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

package org.granite.client.tide.data;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.logging.Logger;

/**
   * @author William DRAI
   */
public class OptimisticLockExceptionHandler implements ExceptionHandler {
      
	private static Logger log = Logger.getLogger("org.granite.tide.data.OptimisticLockExceptionHandler");
  

	@Override
	public boolean accepts(FaultMessage emsg) {
		return emsg.getCode().equals(Code.OPTIMISTIC_LOCK);
	}

	@Override
	public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent) {
		log.debug("optimistic lock error received %s", emsg.toString());
		
		String receivedSessionId = faultEvent.getServerSession().getSessionId() + "_error";
		Object entity = (emsg.getExtended() != null && emsg.getExtended().get("entity") != null) ? emsg.getExtended().get("entity") : null;
  
		// Received entity should be the correct version from the database
        if (entity != null)
        	context.getEntityManager().mergeExternalData(faultEvent.getServerSession(), entity, null, receivedSessionId, null);
      }
  }
