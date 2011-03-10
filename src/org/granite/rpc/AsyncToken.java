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

package org.granite.rpc;

import java.util.ArrayList;
import java.util.List;

import org.granite.rpc.events.AbstractEvent;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AsyncToken {

	private final Message message;
	private final List<AsyncResponder> responders = new ArrayList<AsyncResponder>();
	private AbstractEvent responseEvent;
	
	public AsyncToken(Message message) {
		this.message = message;
	}

	public AsyncToken(Message message, AsyncResponder responder) {
		this.message = message;
		addResponder(responder);
	}

	public Message getMessage() {
		return message;
	}
	
	public void addResponder(AsyncResponder responder) {
		responders.add(responder);
	}

	public List<AsyncResponder> getResponders() {
		return responders;
	}

	public AbstractEvent getResponseEvent() {
		return responseEvent;
	}
	
	public void callResponders(AbstractEvent event) {
		this.responseEvent = event;
		
		if (responseEvent instanceof ResultEvent) {
			ResultEvent resultEvent = (ResultEvent)responseEvent;
			for (AsyncResponder responder : responders)
				responder.result(resultEvent);
		}
		else if (responseEvent instanceof FaultEvent) {
			FaultEvent faultEvent = (FaultEvent)responseEvent;
			for (AsyncResponder responder : responders)
				responder.fault(faultEvent);
		}
		else
			throw new RuntimeException("Unknown event: " + responseEvent);
	}
}
