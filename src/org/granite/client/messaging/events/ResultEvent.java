package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;

public class ResultEvent extends AbstractResponseEvent<ResultMessage> {

	public ResultEvent(RequestMessage request, ResultMessage response) {
		super(request, response);
	}

	@Override
	public Type getType() {
		return Type.RESULT;
	}
	
	public Object getResult() {
		return response.getResult();
	}

	@Override
	public String toString() {
		return getClass().getName() + " {result=" + response.getResult() + "}";
	}
}
