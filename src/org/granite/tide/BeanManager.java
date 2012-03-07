package org.granite.tide;

import java.util.concurrent.Future;

import org.granite.rpc.AsyncToken;


public interface BeanManager {

    public void setProperty(Object bean, String propertyName, Object value);
    
    public Object getProperty(Object bean,  String propertyName);
    
    public <T> Future<T> buildFutureResult(AsyncToken token);
}
