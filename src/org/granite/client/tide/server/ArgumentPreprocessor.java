package org.granite.client.tide.server;

import java.lang.reflect.Method;


public interface ArgumentPreprocessor {
    
    Object[] preprocess(Method method, Object[] args);
}
