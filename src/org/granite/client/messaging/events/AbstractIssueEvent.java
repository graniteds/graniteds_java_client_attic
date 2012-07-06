package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

public abstract class AbstractIssueEvent implements IssueEvent {

	private final RequestMessage request;

	public AbstractIssueEvent(RequestMessage request) {
		this.request = request;
	}
	
	public RequestMessage getRequest() {
		return request;
	}
}
