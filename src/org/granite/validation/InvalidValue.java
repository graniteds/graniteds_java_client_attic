package org.granite.validation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author William DRAI
 */
public class InvalidValue implements Externalizable {

	private Object rootBean;
    private Object bean;
    private Class<?> beanClass;
    private String path;
    private Object value;
    private String message;

    
    public InvalidValue(Object rootBean, Object bean, String path, Object value, String message) {
        if (bean == null || path == null)
            throw new NullPointerException("bean and path parameters cannot be null");
        this.rootBean = rootBean;
        this.bean = bean;
        this.beanClass = bean.getClass();
        this.path = path;
        this.value = value;
        this.message = message;
    }
    
    public Object getRootBean() {
		return rootBean;
	}

	public Object getBean() {
		return bean;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getPath() {
		return path;
	}

	public Object getValue() {
		return value;
	}

	public String getMessage() {
		return message;
	}


	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	rootBean = in.readObject();
    	bean = in.readObject();
    	String beanClassName = (String)in.readObject();
    	if (beanClassName != null)
    		beanClass = Thread.currentThread().getContextClassLoader().loadClass(beanClassName);
    	path = (String)in.readObject();
    	value = in.readObject();
    	message = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    	throw new IOException("Cannot serialize InvalidValue from client");
    }
}
