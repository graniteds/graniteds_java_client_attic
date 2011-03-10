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

package org.granite.rpc.events;

import java.util.Map;

import org.granite.rpc.AsyncToken;

import flex.messaging.messages.ErrorMessage;

/**
 * @author Franck WOLFF
 */
public class FaultEvent extends AbstractEvent {

	private final ErrorMessage response;
	private final Exception exception;
	
	public FaultEvent(AsyncToken token, ErrorMessage response) {
		this(token, response, null);
	}
	
	public FaultEvent(AsyncToken token, Exception exception) {
		this(token, null, exception);
	}

	protected FaultEvent(AsyncToken token, ErrorMessage response, Exception exception) {
		super(token);
		
		if (response == null && exception == null)
			throw new NullPointerException("Response and exception cannot be both null");
		this.response = response;
		this.exception = exception;
	}
	
	public boolean isException() {
		return exception != null;
	}

	public ErrorMessage getResponse() {
		return response;
	}

	public Exception getException() {
		return exception;
	}

    public String getFaultCode() {
        return (isException() ? exception.getClass().getName() : response.getFaultCode());
    }

    public String getFaultDetail() {
        return (isException() ? exception.getMessage() : response.getFaultDetail());
    }

    public String getFaultString() {
        return (isException() ? exception.getMessage() : response.getFaultString());
    }

    public Map<String, Object> getExtendedData() {
        return (isException() ? null : response.getExtendedData());
    }

    public Object getRootCause() {
        return (isException() ? exception.getCause() : response.getRootCause());
    }
}
