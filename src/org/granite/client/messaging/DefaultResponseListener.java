package org.granite.client.messaging;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;

public class DefaultResponseListener implements ResponseListener {

	@Override
	public void onResult(ResultEvent event) {
	}

	@Override
	public void onFault(FaultEvent event) {
	}

	@Override
	public void onFailure(FailureEvent event) {
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
	}

	@Override
	public void onCancelled(CancelledEvent event) {
	}
}
