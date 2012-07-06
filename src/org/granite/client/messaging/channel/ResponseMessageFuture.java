package org.granite.client.messaging.channel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.messages.ResponseMessage;

public interface ResponseMessageFuture {

	public boolean cancel();

	public ResponseMessage get() throws InterruptedException, ExecutionException, TimeoutException;

	public boolean isCancelled();

	public boolean isDone();
}
