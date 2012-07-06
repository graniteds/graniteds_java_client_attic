package org.granite.client.messaging.messages.requests;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.granite.client.messaging.channel.Credentials;

public final class LoginMessage extends AbstractRequestMessage {

	private Credentials credentials;
	
	public LoginMessage() {
	}

	public LoginMessage(String clientId, Credentials credentials) {
		super(clientId);
		
		this.credentials = credentials;
	}

	public LoginMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		Credentials credentials) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.credentials = credentials;
	}

	@Override
	public Type getType() {
		return Type.LOGIN;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	@Override
	public LoginMessage copy() {
		LoginMessage message = new LoginMessage();
		
		copy(message);
		
		message.credentials = credentials;
		
		return message;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		credentials = (Credentials)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeObject(credentials);
	}
	
	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    credentials=").append(credentials);
	}
}
