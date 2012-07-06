package org.granite.client.messaging;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;

public abstract class AllInOneResponseListener implements ResponseListener {

	public abstract void onEvent(Event event);
	
	@Override
	public void onResult(ResultEvent event) {
		onEvent(event);
	}

	@Override
	public void onFault(FaultEvent event) {
		onEvent(event);
	}

	@Override
	public void onFailure(FailureEvent event) {
		onEvent(event);
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		onEvent(event);
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		onEvent(event);
	}
}
