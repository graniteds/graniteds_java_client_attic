package org.granite.tide.client.test;

import org.granite.rpc.AsyncToken;
import org.granite.rpc.events.MessageEvent;
import org.granite.tide.server.Component;


public interface ResponseBuilder {

    public MessageEvent buildResponseEvent(AsyncToken token, Component component, String operation, Object[] args);
}
