package org.granite.client.messaging.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface MessagingCodec<M> {

	String getContentType();
	
	void encode(M message, OutputStream output) throws IOException;
	M decode(InputStream input) throws IOException;
}
