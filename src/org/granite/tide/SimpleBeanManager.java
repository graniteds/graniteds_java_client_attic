package org.granite.tide;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

import org.granite.logging.Logger;
import org.granite.rpc.AsyncToken;
import org.granite.tide.impl.FutureResult;


public class SimpleBeanManager implements BeanManager {
    
    private static final Logger log = Logger.getLogger(SimpleBeanManager.class);

    @Override
    public void setProperty(Object bean, String propertyName, Object value) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(propertyName)) {
                    if (pd.getWriteMethod() == null)
                        log.warn("Bean method " + propertyName + " is not writable ");
                    else
                        pd.getWriteMethod().invoke(bean, value);
                }
            }
        }
        catch (IntrospectionException e) {
            throw new RuntimeException("Could not introspect bean " + bean, e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
    }

    @Override
    public Object getProperty(Object bean, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(propertyName)) {
                    if (pd.getReadMethod() == null)
                        log.warn("Bean method " + propertyName + " is not readable ");
                    else
                        return pd.getReadMethod().invoke(bean);
                }
            }
            return null;
        }
        catch (IntrospectionException e) {
            throw new RuntimeException("Could not introspect bean " + bean, e);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException("Could not write bean property " + bean + "." + propertyName, e);
        }
    }

    @Override
    public <T> Future<T> buildFutureResult(AsyncToken token) {
        return new FutureResult<T>(token);
    }

}
