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

package org.granite.client.tide.javafx;

import java.lang.annotation.ElementType;

import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;

import org.granite.client.tide.data.spi.DataManager;

/**
 * @author William DRAI
 */
public class JavaFXTraversableResolver implements TraversableResolver {
    
    private DataManager dataManager;
    
    public JavaFXTraversableResolver(DataManager dataManager) {
        this.dataManager = dataManager;
    }
	    
    public boolean isReachable(Object bean, Node propertyPath, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
        if (bean == null || propertyPath.getName() == null || ElementType.TYPE.equals(elementType))
            return true;
        Object value = dataManager.getPropertyValue(bean, propertyPath.getName());
        return dataManager.isInitialized(value);
    }
    
    public boolean isCascadable(Object bean, Node propertyPath, Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
        return true;
    }
}
