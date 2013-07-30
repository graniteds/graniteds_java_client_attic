/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.platform.javafx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.granite.messaging.reflect.FieldProperty;
import org.granite.messaging.reflect.MethodProperty;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class JavaFXReflection extends Reflection {

	public JavaFXReflection(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	protected FieldProperty newFieldProperty(Field field) {
		return new JavaFXFieldProperty(field);
	}

	@Override
	protected MethodProperty newMethodProperty(Method getter, Method setter, String name) {
		return new JavaFXMethodProperty(getter, setter, name);
	}
}
