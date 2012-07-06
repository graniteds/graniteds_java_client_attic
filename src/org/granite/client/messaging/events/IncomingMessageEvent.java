package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.Message;

public interface IncomingMessageEvent<M extends Message> extends Event {

	M getMessage();
}
