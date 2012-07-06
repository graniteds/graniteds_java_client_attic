package org.granite.client.messaging;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.TimeoutEvent;

public abstract class ResultFaultIssuesResponseListener implements ResponseListener {

	public abstract void onIssue(IssueEvent event);

	@Override
	public void onFailure(FailureEvent event) {
		onIssue(event);
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		onIssue(event);
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		onIssue(event);
	}
}
