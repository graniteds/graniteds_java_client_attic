package org.granite.client.test;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.messages.Message;
import org.granite.client.messaging.messages.RequestMessage;


public interface ResponseBuilder {

    public Message buildResponseMessage(RemoteService service, RequestMessage request);
}
