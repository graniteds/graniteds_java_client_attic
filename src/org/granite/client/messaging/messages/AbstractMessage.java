package org.granite.client.messaging.messages;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.granite.util.UUIDUtil;

public abstract class AbstractMessage implements Message {
	
    private String id = UUIDUtil.randomUUID();
    private String clientId = null;

	private long timestamp = 0L;
    private long timeToLive = 0L;
	
	private Map<String, Object> headers = new HashMap<String, Object>();
	
	public AbstractMessage() {
	}

	public AbstractMessage(String clientId) {
		this.clientId = clientId;
	}

	public AbstractMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers) {

		setId(id);
		this.clientId = clientId;
		this.timestamp = timestamp;
		this.timeToLive = timeToLive;
		this.headers = headers;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("id cannot be null");
		this.id = id;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public long getTimeToLive() {
		return timeToLive;
	}

	@Override
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	@Override
	public Map<String, Object> getHeaders() {
		return headers;
	}

	@Override
	public void setHeaders(Map<String, Object> headers) {
		if (headers == null)
			throw new NullPointerException("headers cannot be null");
		this.headers = headers;
	}

	@Override
	public Object getHeader(String name) {
		return headers;
	}

	@Override
	public void setHeader(String name, Object value) {
		headers.put(name, value);
	}

	@Override
	public boolean headerExists(String name) {
		return headers.containsKey(name);
	}

	@Override
	public boolean isExpired() {
		return isExpired(System.currentTimeMillis());
	}

	@Override
	public boolean isExpired(long currentTimeMillis) {
		return getRemainingTimeToLive(currentTimeMillis) < 0;
	}

	@Override
	public long getRemainingTimeToLive() {
		return getRemainingTimeToLive(System.currentTimeMillis());
	}

	@Override
	public long getRemainingTimeToLive(long currentTimeMillis) {
		if (timestamp <= 0L || this.timeToLive <= 0L)
			throw new IllegalStateException("Unset timestamp/timeToLive: {timestamp=" + timestamp + ", timeToLive=" + timeToLive + "}");
		return timestamp + timeToLive - currentTimeMillis;
	}
	
	protected void copy(AbstractMessage message) {
		message.id = id;
		message.clientId = clientId;
		message.timestamp = timestamp;
		message.timeToLive = timeToLive;
		message.headers.putAll(headers);
	}

	@Override
	public Message clone() throws CloneNotSupportedException {
		return copy();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {		
	    String id = in.readUTF();
	    if (id == null)
	    	throw new RuntimeException("Message id cannot be null");
	    this.id = id;
	    
	    clientId = in.readUTF();

		timestamp = in.readLong();
	    timeToLive = in.readLong();

	    Map<String, Object> headers = (Map<String, Object>)in.readObject();
		if (headers != null)
			this.headers = headers;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	    out.writeUTF(id);
	    if (clientId != null)
	    	out.writeUTF(clientId);
	    else
	    	out.writeObject(null);

		out.writeLong(timestamp);
		out.writeLong(timeToLive);
		
		if (headers.size() > 0)
			out.writeObject(headers);
		else
			out.writeObject(null);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof AbstractMessage && id.equals(((AbstractMessage)obj).id));
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder(getClass().getName()).append(" {")).append("\n}").toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		return sb
			.append("\n    id=").append(id)
			.append("\n    clientId=").append(clientId)
			.append("\n    timestamp=").append(timestamp)
			.append("\n    timeToLive=").append(timeToLive)
			.append("\n    headers=").append(headers);
	}
}
