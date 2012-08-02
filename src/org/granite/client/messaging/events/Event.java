package org.granite.client.messaging.events;

public interface Event {

	public static enum Type {
		RESULT,
		FAULT,
		FAILURE,
		TIMEOUT,
		CANCELLED,
		
		TOPIC
	}
	
	Type getType();
}
