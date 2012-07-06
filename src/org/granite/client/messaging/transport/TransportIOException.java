package org.granite.client.messaging.transport;

public class TransportIOException extends TransportException {

	private static final long serialVersionUID = 1L;
	
	private final Object data;

	public TransportIOException(Object data) {
		this(data, null, null);
	}

	public TransportIOException(Object data, String message) {
		this(data, message, null);
	}

	public TransportIOException(Object data, Throwable cause) {
		this(data, null, cause);
	}

	public TransportIOException(Object data, String message, Throwable cause) {
		super(message, cause);
		this.data = data;
	}

	public Object getData() {
		return data;
	}
}
