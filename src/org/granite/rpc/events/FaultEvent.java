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
public class FaultEvent extends MessageEvent {

	private final Exception exception;
	
	public FaultEvent(AsyncToken token, ErrorMessage message) {
		this(token, message, null);
	}
	
	public FaultEvent(AsyncToken token, Exception exception) {
		this(token, null, exception);
	}

	protected FaultEvent(AsyncToken token, ErrorMessage message, Exception exception) {
		super(token, message);
		
		if (message == null && exception == null)
			throw new NullPointerException("Response and exception cannot be both null");
		this.exception = exception;
	}
	
	public boolean isException() {
		return exception != null;
	}

	public ErrorMessage getMessage() {
		return (ErrorMessage)super.getMessage();
	}

	public Exception getException() {
		return exception;
	}

    public String getFaultCode() {
        return (isException() ? exception.getClass().getName() : getMessage().getFaultCode());
    }

    public String getFaultDetail() {
        return (isException() ? exception.getMessage() : getMessage().getFaultDetail());
    }

    public String getFaultString() {
        return (isException() ? exception.getMessage() : getMessage().getFaultString());
    }

    public Map<String, Object> getExtendedData() {
        return (isException() ? null : getMessage().getExtendedData());
    }

    public Object getRootCause() {
        return (isException() ? exception.getCause() : getMessage().getRootCause());
    }
    
    @Override
    public String toString() {
    	return getClass().getSimpleName() + ": " + getMessage() + " fault: " + getFaultCode() + " " + getFaultString(); 
    }
}
