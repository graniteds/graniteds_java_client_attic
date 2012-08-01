package org.granite.client.tide.server;

import java.util.concurrent.Future;


public interface Component {
    
    public String getName();
    
    public <T> Future<T> call(String operation, Object... args);

}
