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

package org.granite.client.tide.data.impl;

/**
 * @author William DRAI
 */
public class PropertyChange {

    private Object object;
    private String propertyName;
    private Object oldValue;
    private Object newValue;
    
    public PropertyChange(Object object, String propertyName, Object oldValue, Object newValue) {
        this.object = object;
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;         
    }
    
    public Object getObject() {
        return object;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
}
