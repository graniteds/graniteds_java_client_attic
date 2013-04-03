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

package org.granite.client.tide.impl;

import java.lang.reflect.Method;

import org.granite.client.tide.BeanManager;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
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

}
