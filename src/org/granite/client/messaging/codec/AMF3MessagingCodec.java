package org.granite.client.messaging.codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.Channel;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;

import flex.messaging.messages.Message;

public class AMF3MessagingCodec implements MessagingCodec<Message[]> {

	private final Configuration config;
	
	public AMF3MessagingCodec(Configuration config) {
		this.config = config;
	}

	@Override
	public String getContentType() {
		return "application/x-amf";
	}

	@Override
	public void encode(Message[] messages, OutputStream output) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0));
		try {
			AMF3Serializer serializer = new AMF3Serializer(output);
			serializer.writeObject(messages);
		}
		finally {
			GraniteContext.release();
		}
	}

	@Override
	public Message[] decode(InputStream input) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0));
		try {
			AMF3Deserializer deserializer = new AMF3Deserializer(input);
			Object[] objects = (Object[])deserializer.readObject();
			if (objects != null) {
				Message[] messages = new Message[objects.length];
				System.arraycopy(objects, 0, messages, 0, objects.length);
				
				for (Message message : messages) {
					if (Boolean.TRUE.equals(message.getHeader(Channel.BYTEARRAY_BODY_HEADER))) {
						byte[] body = (byte[])message.getBody();
						message.setBody(new AMF3Deserializer(new ByteArrayInputStream(body)).readObject());
					}
				}
				
				return messages;
			}
			return new Message[0];
		}
		finally {
			GraniteContext.release();
		}
	}
}
