package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;

public interface IssueEvent extends Event {

	RequestMessage getRequest();
}
