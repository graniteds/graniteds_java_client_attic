package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.messaging.transport.Transport;

public abstract class AbstractChannel<T extends Transport> implements Channel {
	
	protected final T transport;
	protected final String id;
	protected final URI uri;
	
	protected volatile String clientId;

	protected volatile Credentials credentials = null;
	protected volatile Object transportData = null;
	
	public AbstractChannel(T transport, String id, URI uri) {
		if (transport == null || id == null || uri == null)
			throw new NullPointerException("Engine, id and uri must be not null");
		
		this.transport = transport;
		this.id = id;
		this.uri = uri;
	}

	@Override
	public T getTransport() {
		return transport;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	@Override
	public Credentials getCredentials() {
		return credentials;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D> D getTransportData() {
		return (D)transportData;
	}

	@Override
	public void setTransportData(Object data) {
		this.transportData = data;
	}
}
