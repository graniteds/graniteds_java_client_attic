package org.granite.tide.client.test;

import org.granite.client.rpc.AsyncToken;
import org.granite.client.rpc.events.MessageEvent;
import org.granite.client.tide.server.Component;


public interface ResponseBuilder {

    public MessageEvent buildResponseEvent(AsyncToken token, Component component, String operation, Object[] args);
}
