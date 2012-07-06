package org.granite.client.messaging.messages;

import java.io.Externalizable;
import java.util.Map;

public interface Message extends Externalizable, Cloneable {

	public static enum Type {
		
		// Request types.
		PING,
		LOGIN,
		LOGOUT,
		INVOCATION,
		SUBSCRIBE,
		UNSUBSCRIBE,
		PUBLISH,
		
		// Response types.
		RESULT,
		FAULT,
		
		// Push types.
		POLL,
		PUSH
	}
    
    Type getType();
	
	String getId();
    void setId(String id);
	
	String getClientId();
    void setClientId(String clientId);

	long getTimestamp();
    void setTimestamp(long timestamp);
	
    long getTimeToLive();
    void setTimeToLive(long timeToLive);

    Map<String, Object> getHeaders();
    void setHeaders(Map<String, Object> headers);
	Object getHeader(String name);
    void setHeader(String name, Object value);
	boolean headerExists(String name);
    
    boolean isExpired();
    boolean isExpired(long millis);
    long getRemainingTimeToLive();
    long getRemainingTimeToLive(long millis);
    
    public Message copy();
    public Message clone() throws CloneNotSupportedException;
}
