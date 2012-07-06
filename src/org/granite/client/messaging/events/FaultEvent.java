package org.granite.client.messaging.events;

import java.util.Map;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;

public class FaultEvent extends AbstractResponseEvent<FaultMessage> implements IssueEvent {

	public FaultEvent(RequestMessage request, FaultMessage response) {
		super(request, response);
	}

	@Override
	public Type getType() {
		return Type.FAULT;
	}
	
	public Code getCode() {
		return response.getCode();
	}

	public String getDescription() {
		return response.getDescription();
	}

	public String getDetails() {
		return response.getDetails();
	}

	public Object getCause() {
		return response.getCause();
	}

	public Map<String, Object> getExtended() {
		return response.getExtended();
	}

	public String getUnknownCode() {
		return response.getUnknownCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + "{code=" + getCode() +
			", description=" + getDescription() +
			", details=" + getDetails() +
			", cause=" + getCause() +
			", extended=" + getExtended() +
			", unknownCode=" + getUnknownCode() +
		"}";
	}
}
