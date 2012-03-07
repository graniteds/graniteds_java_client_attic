package org.granite.tide;

import java.lang.reflect.Method;


public interface ArgumentPreprocessor {
    
    Object[] preprocess(Method method, Object[] args);
}
