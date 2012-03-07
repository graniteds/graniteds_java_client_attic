package org.granite.tide;

import java.util.concurrent.Future;

import org.granite.rpc.AsyncToken;
import org.granite.tide.rpc.ComponentResponder;


public interface Component {
    
    public String getName();
    
    public <T> Future<T> call(String operation, Object... args);
    
    public AsyncToken invoke(ComponentResponder componentResponder);

}
