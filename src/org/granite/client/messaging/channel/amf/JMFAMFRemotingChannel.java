package org.granite.client.messaging.channel.amf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.codec.JMFAMF0MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.transport.DefaultTransportMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;

public class JMFAMFRemotingChannel extends AMFRemotingChannel {

	private final ClientSharedContext sharedContext;
	
	public JMFAMFRemotingChannel(Transport transport, String id, URI uri, ClientSharedContext sharedContext) {
		super(transport, id, uri);
		
		this.sharedContext = sharedContext;
	}

	public JMFAMFRemotingChannel(Transport transport, String id, URI uri, ClientSharedContext sharedContext, int maxConcurrentRequests) {
		super(transport, id, uri, maxConcurrentRequests);
		
		this.sharedContext = sharedContext;
	}

	public JMFAMFRemotingChannel(Transport transport, Configuration configuration, String id, URI uri, ClientSharedContext sharedContext, int maxConcurrentRequests) {
		super(transport, configuration, id, uri, maxConcurrentRequests);
		
		this.sharedContext = sharedContext;
	}

	@Override
	protected MessagingCodec<AMF0Message> newMessagingCodec(Configuration configuration) {
		return new JMFAMF0MessagingCodec(configuration, sharedContext);
	}
	
	@Override
	protected TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException {
		AMF0Message amf0Message = new AMF0Message();
		for (Message message : convertToAmf(token.getRequest())) {
		    AMF0Body body = new AMF0Body("", "/" + (index++), new Object[]{message}, AMF0Body.DATA_TYPE_AMF3_OBJECT);
		    amf0Message.addBody(body);
		}
		return new DefaultTransportMessage<AMF0Message>(token.getId(), false, clientId, null, amf0Message, codec);
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		final AMF0Message amf0Message = codec.decode(is);
		final int messagesCount = amf0Message.getBodyCount();
		
		AbstractResponseMessage response = null, previous = null;
		
		for (int i = 0; i < messagesCount; i++) {
			AMF0Body body = amf0Message.getBody(i);
			
			if (!(body.getValue() instanceof AMF3Object))
				throw new RuntimeException("Message should be an AMF3Object: " + body.getValue());
			
			AMF3Object bodyObject = (AMF3Object)body.getValue();
			if (!(bodyObject.getValue() instanceof AcknowledgeMessage))
				throw new RuntimeException("Message should be an AcknowledgeMessage: " + bodyObject.getValue());
			
			AcknowledgeMessage message = (AcknowledgeMessage)bodyObject.getValue();
			AbstractResponseMessage current = convertFromAmf(message);
			
			if (response == null)
				response = previous = current;
			else {
				previous.setNext(current);
				previous = current;
			}
		}
		
		return response;
	}
}
