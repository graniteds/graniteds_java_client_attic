package org.granite.client.messaging.transport;

import java.io.IOException;
import java.io.OutputStream;


public interface TransportMessage {

	String getId();
	
	boolean isConnect();
	
	String getClientId();
	
	String getSessionId();
	
	String getContentType();
	
	void encode(OutputStream os) throws IOException;
}
