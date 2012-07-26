package org.granite.client.tide;

import java.util.concurrent.Future;

import org.granite.client.messaging.channel.ResponseMessageFuture;


public interface BeanManager {

    public void setProperty(Object bean, String propertyName, Object value);
    
    public Object getProperty(Object bean,  String propertyName);
    
    public <T> Future<T> buildFutureResult(ResponseMessageFuture rmfuture);
}
