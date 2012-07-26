package org.granite.client.messaging.channel;

import java.io.InputStream;
import java.net.URI;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;

public interface Channel {
	
    public static final String RECONNECT_INTERVAL_MS_KEY = "reconnect-interval-ms";
    public static final String RECONNECT_MAX_ATTEMPTS_KEY = "reconnect-max-attempts";
    
    public static final String BYTEARRAY_BODY_HEADER = "GDS_BYTEARRAY_BODY";    

	Transport getTransport();
	String getId();
	URI getUri();
	String getClientId();
	
	boolean start();
	boolean isStarted();
	boolean stop();
	
	void setCredentials(Credentials credentials);
	Credentials getCredentials();
	boolean isAuthenticated();
	
	ResponseMessageFuture send(RequestMessage request, ResponseListener... listeners);
	ResponseMessageFuture logout(ResponseListener... listeners);
	
	<D> D getTransportData();
	void setTransportData(Object data);

	void onMessage(InputStream is);
	void onError(TransportMessage message, Exception e);
	void onCancelled(TransportMessage message);
}
