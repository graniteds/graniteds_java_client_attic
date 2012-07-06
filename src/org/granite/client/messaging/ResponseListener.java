package org.granite.client.messaging;

import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;

public interface ResponseListener {

	void onResult(ResultEvent event);
	void onFault(FaultEvent event);
	void onFailure(FailureEvent event);
	void onTimeout(TimeoutEvent event);
	void onCancelled(CancelledEvent event);
}
