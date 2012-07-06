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

package org.granite.client.rpc.events;

import org.granite.client.rpc.AsyncToken;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class MessageEvent {

	private final AsyncToken token;
	private final Message message;
	
	public MessageEvent(AsyncToken token, Message message) {
		if (token == null)
			throw new NullPointerException("Token cannot be null");
		this.token = token;
		this.message = message;
	}

	public AsyncToken getToken() {
		return token;
	}
	
	public Message getMessage() {
	    return message;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + message;
	}
}
