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

package org.granite.client.messaging.jmf.ext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Persistence;
import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;

/**
 * @author Franck WOLFF
 */
public class ClientEntityCodec implements ExtendedObjectCodec {

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v.getClass().isAnnotationPresent(Entity.class);
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
        return out.getAlias(v.getClass().getName());
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		boolean initialized = Persistence.isInitialized(v);
		
		out.writeBoolean(initialized);
		out.writeUTF(Persistence.getDetachedState(v));
		
		if (!initialized)
			out.writeObject(Persistence.getId(v));
		else {
			List<Field> fields = new ArrayList<Field>(out.getReflection().findSerializableFields(v.getClass()));
			fields.remove(Persistence.getInitializedField(v.getClass()));
			fields.remove(Persistence.getDetachedStateField(v.getClass()));

			for (Field field : fields)
				out.getAndWriteField(v, field);
		}
	}

	public boolean canDecode(ExtendedObjectInput in, String className) throws ClassNotFoundException {
		String alias = in.getAlias(className);
		Class<?> cls = in.getReflection().loadClass(alias);
		return cls.isAnnotationPresent(Entity.class);
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return in.getAlias(className);
	}

	public Object newInstance(ExtendedObjectInput in, String className)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
		
		Class<?> cls = in.getReflection().loadClass(className);
		return in.getReflection().newInstance(cls);
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		
		boolean initialized = in.readBoolean();
		String detachedState = in.readUTF();
		
		Persistence.setInitialized(v, initialized);
		Persistence.setDetachedState(v, detachedState);
		
		if (!initialized)
			Persistence.setId(v, in.readObject());
		else {
			List<Field> fields = new ArrayList<Field>(in.getReflection().findSerializableFields(v.getClass()));
			fields.remove(Persistence.getInitializedField(v.getClass()));
			fields.remove(Persistence.getDetachedStateField(v.getClass()));

			for (Field field : fields)
				in.readAndSetField(v, field);
		}
	}
}