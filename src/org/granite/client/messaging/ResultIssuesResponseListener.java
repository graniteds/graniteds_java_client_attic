package org.granite.client.messaging;

import org.granite.client.messaging.events.FaultEvent;

public abstract class ResultIssuesResponseListener extends ResultFaultIssuesResponseListener {

	@Override
	public void onFault(FaultEvent event) {
		onIssue(event);
	}
}
