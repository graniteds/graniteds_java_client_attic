package org.granite.client.messaging.messages.responses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

public final class ResultMessage extends AbstractResponseMessage {

	private Object result;
	
	public ResultMessage() {
	}

	public ResultMessage(String clientId, String correlationId, Object result) {
		super(clientId, correlationId);
		
		this.result = result;
	}

	public ResultMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String correlationId,
		Object result) {
		
		super(id, clientId, timestamp, timeToLive, headers, correlationId);
		
		this.result = result;
	}

	@Override
	public Type getType() {
		return Type.RESULT;
	}

	@Override
	public Object getData() {
		return result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@Override
	public ResultMessage copy() {
		ResultMessage message = new ResultMessage();

		super.copy(message);
		
		message.result = result;
		
		return message;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		this.result = in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeObject(result);
	}

	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    result=").append(result);
	}
}
