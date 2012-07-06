package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

public class FailureEvent extends AbstractIssueEvent {

	private final Exception cause;

	public FailureEvent(RequestMessage request, Exception cause) {
		super(request);
		this.cause = cause;
	}

	@Override
	public Type getType() {
		return Type.FAILURE;
	}

	public Exception getCause() {
		return cause;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {cause=" + cause + "}";
	}
}
