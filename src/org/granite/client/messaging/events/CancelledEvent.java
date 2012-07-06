package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

public class CancelledEvent extends AbstractIssueEvent {

	public CancelledEvent(RequestMessage request) {
		super(request);
	}

	@Override
	public Type getType() {
		return Type.CANCELLED;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {}";
	}
}
