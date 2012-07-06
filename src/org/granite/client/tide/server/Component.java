package org.granite.client.tide.server;

import org.granite.client.messaging.channel.ResponseMessageFuture;


public interface Component {
    
    public String getName();
    
    public ResponseMessageFuture call(String operation, Object... args);

}
