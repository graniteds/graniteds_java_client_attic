package org.granite.client.messaging.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.granite.client.configuration.Configuration;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;

public class AMF0MessagingCodec implements MessagingCodec<AMF0Message> {

	private final Configuration config;
	
	public AMF0MessagingCodec(Configuration config) {
		this.config = config;
	}

	@Override
	public String getContentType() {
		return "application/x-amf";
	}

	@Override
	public void encode(AMF0Message message, OutputStream output) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0));
		try {
			AMF0Serializer serializer = new AMF0Serializer(output);
			serializer.serializeMessage(message);
		}
		finally {
			GraniteContext.release();
		}
	}

	@Override
	public AMF0Message decode(InputStream input) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0));
		try {
			AMF0Deserializer deserializer = new AMF0Deserializer(input);
			return deserializer.getAMFMessage();
		}
		finally {
			GraniteContext.release();
		}
	}
}
