package org.granite.client.messaging.messages.responses;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.ResponseMessage;

public abstract class AbstractResponseMessage extends AbstractMessage implements ResponseMessage {

	private String correlationId;
	private ResponseMessage next;
	
	public AbstractResponseMessage() {
	}

	public AbstractResponseMessage(String clientId, String correlationId) {
		super(clientId);
		
		this.correlationId = correlationId;
	}

	public AbstractResponseMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String correlationId) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.correlationId = correlationId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
	
	@Override
	public void setNext(ResponseMessage next) {
		for (ResponseMessage n = next; n != null; n = n.getNext()) {
			if (n == this)
				throw new RuntimeException("Circular chaining to this: " + next);
		}
		this.next = next;
	}

	@Override
	public ResponseMessage getNext() {
		return next;
	}
	
	@Override
	public Iterator<ResponseMessage> iterator() {
		
		final ResponseMessage first = this;
		
		return new Iterator<ResponseMessage>() {

			private ResponseMessage current = first;
			
			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public ResponseMessage next() {
				if (current == null)
					throw new NoSuchElementException();
				ResponseMessage c = current;
				current = current.getNext();
				return c;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		this.correlationId = in.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		if (correlationId != null)
			out.writeUTF(correlationId);
		else
			out.writeObject(null);
	}

	@Override
	protected void copy(AbstractMessage message) {
		copy((AbstractResponseMessage)message, correlationId);
	}

	protected void copy(AbstractResponseMessage message, String correlationId) {
		super.copy(message);
		
		message.correlationId = correlationId;
	}

	public ResponseMessage copy(String correlationId) {
		AbstractResponseMessage message = (AbstractResponseMessage)copy();
		
		message.correlationId = correlationId;
		
		return message;
	}

	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    correlationId=").append(correlationId);
	}
}
