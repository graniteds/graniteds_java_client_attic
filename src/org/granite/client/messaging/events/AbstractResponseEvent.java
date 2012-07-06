package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.ResponseMessage;

public abstract class AbstractResponseEvent<M extends ResponseMessage> implements IncomingMessageEvent<M> {

	protected final RequestMessage request;
	protected final M response;
	
	public AbstractResponseEvent(RequestMessage request, M response) {
		if (request == null || response == null)
			throw new NullPointerException("request and response cannot be null");

		this.request = request;
		this.response = response;
	}

	public RequestMessage getRequest() {
		return request;
	}

	public M getResponse() {
		return response;
	}

	@Override
	public M getMessage() {
		return response;
	}
}