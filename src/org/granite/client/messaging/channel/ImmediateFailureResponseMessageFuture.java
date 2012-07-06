package org.granite.client.messaging.channel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.messages.ResponseMessage;

public class ImmediateFailureResponseMessageFuture implements ResponseMessageFuture {

	private final Exception cause;
	
	public ImmediateFailureResponseMessageFuture(Exception cause) {
		if (cause == null)
			throw new NullPointerException("cause cannot be null");
		this.cause = cause;
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public ResponseMessage get() throws InterruptedException, ExecutionException, TimeoutException {
		throw new ExecutionException(cause);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}
}
