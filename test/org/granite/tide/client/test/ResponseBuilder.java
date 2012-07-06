package org.granite.tide.client.test;

import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.tide.server.Component;


public interface ResponseBuilder {

    public Object buildResponseEvent(AsyncToken token, Component component, String operation, Object[] args);
}
