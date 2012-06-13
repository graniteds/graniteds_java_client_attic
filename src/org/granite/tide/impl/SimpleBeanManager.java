package org.granite.tide.impl;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

import org.granite.logging.Logger;
import org.granite.rpc.AsyncToken;
import org.granite.tide.BeanManager;


public class SimpleBeanManager implements BeanManager {
    
    @SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimpleBeanManager.class);

    @Override
    public void setProperty(Object bean, String propertyName, Object value) {
        try {
        	boolean found = false;
        	for (Method m : bean.getClass().getMethods()) {
        		if (m.getName().equals("set" + propertyName.substring(0, 1).toString() + propertyName.substring(1))
        				&& m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isInstance(value)) {
        			m.invoke(bean, value);
        			found = true;
        			break;
        		}
        	}
        	if (!found)
        		throw new RuntimeException("Could not find setter for bean property " + bean + "." + propertyName);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
    }

    @Override
    public Object getProperty(Object bean, String propertyName) {
        try {
        	for (Method m : bean.getClass().getMethods()) {
        		if ((m.getName().equals("get" + propertyName.substring(0, 1).toString() + propertyName.substring(1))
        				|| m.getName().equals("is" + propertyName.substring(0, 1).toString() + propertyName.substring(1)))
        				&& m.getParameterTypes().length == 0) {
        			return m.invoke(bean);
        		}
        	}
        	throw new RuntimeException("Could not find getter for bean property " + bean + "." + propertyName);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not read bean property " + bean + "." + propertyName, e);
        }
    }

    @Override
    public <T> Future<T> buildFutureResult(AsyncToken token) {
        return new FutureResult<T>(token);
    }

}
