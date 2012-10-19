/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.validation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.messaging.amf.RemoteClass;

/**
 * @author William DRAI
 */
@RemoteClass("org.granite.tide.validators.InvalidValue")
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
    	in.readObject();
    	path = (String)in.readObject();
    	value = in.readObject();
    	message = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    	throw new IOException("Cannot serialize InvalidValue from client");
    }
}
