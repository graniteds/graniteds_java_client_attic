package org.granite.client.tide.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;


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
