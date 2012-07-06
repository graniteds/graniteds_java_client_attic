package org.granite.client.messaging.transport;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.transport.TransportStatusHandler.LogEngineStatusHandler;
import org.granite.client.messaging.transport.TransportStatusHandler.NoopEngineStatusHandler;

public abstract class AbstractTransport implements Transport {

	private Configuration config;
	private TransportStatusHandler statusHandler = new LogEngineStatusHandler();
	
	private final List<TransportStopListener> stopListeners = new ArrayList<TransportStopListener>();
	
	@Override
	public void setConfiguration(Configuration config) {
		this.config = config;
	}

	@Override
	public Configuration getConfiguration() {
		return config;
	}

	@Override
	public void setStatusHandler(TransportStatusHandler statusHandler) {
		if (statusHandler == null)
			statusHandler = new NoopEngineStatusHandler();
		this.statusHandler = statusHandler;
	}

	@Override
	public TransportStatusHandler getStatusHandler() {
		return statusHandler;
	}

	@Override
	public void addStopListener(TransportStopListener listener) {
		synchronized (stopListeners) {
			if (!stopListeners.contains(listener))
				stopListeners.add(listener);
		}
	}

	@Override
	public boolean removeStopListener(TransportStopListener listener) {
		synchronized (stopListeners) {
			return stopListeners.remove(listener);
		}
	}

	@Override
	public void stop() {
		synchronized (stopListeners) {
			for (TransportStopListener listener : stopListeners) {
				try {
					listener.onStop(this);
				}
				catch (Exception e) {
				}
			}
			stopListeners.clear();
		}
	}
}
