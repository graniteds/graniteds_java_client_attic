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

package org.granite.client.tide.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;

/**
 * @author William DRAI
 */
public class FutureResult<T> implements Future<T> {
    
    private ResponseMessageFuture rmfuture;
    
    public FutureResult(ResponseMessageFuture rmfuture) {
    	this.rmfuture = rmfuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return rmfuture.cancel();
    }

    @Override
    public boolean isCancelled() {
        return rmfuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return rmfuture.isDone();
    }

    @SuppressWarnings("unchecked")
	@Override
    public T get() throws InterruptedException, ExecutionException {
    	try {
	    	ResponseMessage message = rmfuture.get();
	    	if (message instanceof ResultMessage)
	    		return (T)((ResultMessage)message).getResult();
	    	
	    	// TODO: got a forced timeout
	    	throw new RuntimeException(((FaultMessage)message).getDescription());
    	}
    	catch (TimeoutException e) {
    		throw new RuntimeException("Invocation timeout", e);
    	}
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    	throw new UnsupportedOperationException("Not supported: you should specify a timeout on the RemoteService invocation");
    }

}
