package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

public class TimeoutEvent extends AbstractIssueEvent {

	private final long time;
	
	public TimeoutEvent(RequestMessage request, long time) {
		super(request);
		
		this.time = time;
	}

	@Override
	public Type getType() {
		return Type.TIMEOUT;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return getClass().getName() + " {timestamp=" + getRequest().getTimestamp() + " + timeToLive=" + getRequest().getTimeToLive() + " > time=" + time + "}";
	}
}
