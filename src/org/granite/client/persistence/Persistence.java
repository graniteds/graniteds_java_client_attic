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

package org.granite.client.persistence;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.granite.client.persistence.collection.PersistentCollection;

/**
 * @author Franck WOLFF
 */
public class Persistence {

	private static final String INITIALIZED_FIELD_NAME = "__initialized__";
	private static final String DETACHED_STATE_FIELD_NAME = "__detachedState__";
	
	private static final PropertyAccessor NULL_PROPERTY_ACCESSOR = new MethodAccessor(null, null, null);
	
	private static final ConcurrentMap<Class<?>, PropertyAccessor> idAccessors = new ConcurrentHashMap<Class<?>, PropertyAccessor>();
	private static final ConcurrentMap<Class<?>, PropertyAccessor> uidAccessors = new ConcurrentHashMap<Class<?>, PropertyAccessor>();
	private static final ConcurrentMap<Class<?>, PropertyAccessor> versionAccessors = new ConcurrentHashMap<Class<?>, PropertyAccessor>();
	
	private static final Field NULL_FIELD;
	static {
		try {
			NULL_FIELD = Persistence.class.getDeclaredField("NULL_FIELD");
		}
		catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private static final ConcurrentMap<Class<?>, Field> initializedFields = new ConcurrentHashMap<Class<?>, Field>();
	private static final ConcurrentMap<Class<?>, Field> detachedStateFields = new ConcurrentHashMap<Class<?>, Field>();

	public static Field getInitializedField(Class<?> cls) {
		return findField(cls, initializedFields, INITIALIZED_FIELD_NAME, Boolean.TYPE);
	}

	public static Field getDetachedStateField(Class<?> cls) {
		return findField(cls, detachedStateFields, DETACHED_STATE_FIELD_NAME, String.class);
	}
	
	public static boolean isInitialized(Object o) {
		if (o instanceof PersistentCollection)
			return ((PersistentCollection)o).wasInitialized();
		
		Class<?> cls = o.getClass();
		if (!cls.isAnnotationPresent(Entity.class))
			return true;

		Field field = findField(cls, initializedFields, INITIALIZED_FIELD_NAME, Boolean.TYPE);
		if (field == null)
			return true;
		
		try {
			return field.getBoolean(o);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not get field " + field + " value on object " + o, e);
		}
	}
	
	public static void setInitialized(Object o, boolean value) throws IllegalAccessException {
		Class<?> cls = o.getClass();
		if (!cls.isAnnotationPresent(Entity.class))
			throw new IllegalAccessException("Not annotated with @Entity: " + cls);
		
		Field field = findField(cls, initializedFields, INITIALIZED_FIELD_NAME, Boolean.TYPE);
		if (field == null)
			throw new IllegalAccessException("Could not find field " + INITIALIZED_FIELD_NAME + " in " + o);
		
		try {
			field.setBoolean(o, value);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not set field " + field + " on object " + o + " to value " + value, e);
		}		
	}
	
	public static String getDetachedState(Object o) throws IllegalAccessException {
		Class<?> cls = o.getClass();
		if (!cls.isAnnotationPresent(Entity.class))
			return null;

		Field field = findField(cls, detachedStateFields, DETACHED_STATE_FIELD_NAME, String.class);
		if (field == null)
			throw new IllegalAccessException("Could not find field " + DETACHED_STATE_FIELD_NAME + " in " + o);
		
		return (String)field.get(o);
	}
	
	public static void setDetachedState(Object o, String value) throws IllegalAccessException {
		Class<?> cls = o.getClass();
		if (!cls.isAnnotationPresent(Entity.class))
			throw new IllegalAccessException("Not annotated with @Entity: " + cls);
		
		Field field = findField(cls, detachedStateFields, DETACHED_STATE_FIELD_NAME, String.class);
		if (field == null)
			throw new IllegalAccessException("Could not find field " + DETACHED_STATE_FIELD_NAME + " in " + o);
		
		try {
			field.set(o, value);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not set field " + field + " on object " + o + " to value " + value, e);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getId(Object entity) throws IllegalAccessException {
		return (T)getIdProperty(entity).getValue();
	}

	public static void setId(Object entity, Object value) throws IllegalAccessException {
		getIdProperty(entity).setValue(value);
	}
	
	public static String getUid(Object entity) throws IllegalAccessException {
		return (String)getUidProperty(entity).getValue();
	}
	
	public static void setUid(Object entity, String value) throws IllegalAccessException {
		getUidProperty(entity).setValue(value);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getVersion(Object entity) throws IllegalAccessException {
		return (T)getVersionProperty(entity).getValue();
	}

	public static Property getIdProperty(Object entity) throws IllegalAccessException {
		return getProperty(entity, idAccessors, Id.class);
	}

	public static Property getUidProperty(Object entity) throws IllegalAccessException {
		return getProperty(entity, uidAccessors, Uid.class);
	}

	public static Property getVersionProperty(Object entity) throws IllegalAccessException {
		return getProperty(entity, versionAccessors, Version.class);
	}
	
	private static Property getProperty(Object entity, ConcurrentMap<Class<?>, PropertyAccessor> cache, Class<? extends Annotation> annotationClass) throws IllegalAccessException {
		PropertyAccessor accessor = findPropertyAccessor(entity.getClass(), cache, annotationClass);
		if (accessor == null)
			throw newIllegalAccessException(entity.getClass(), annotationClass);
		return new Property(entity, accessor);
	}
	
	private static IllegalAccessException newIllegalAccessException(Class<?> cls, Class<? extends Annotation> annotationClass) {
		return new IllegalAccessException("Could not find property annotated with " + annotationClass + " in " + cls);
	}
	
	private static Field findField(Class<?> cls, ConcurrentMap<Class<?>, Field> cache, String name, Class<?> type) {
		
		Field field = cache.get(cls);
		
		if (field == null) {
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
				try {
					field = c.getDeclaredField(name);
				}
				catch (Exception e) {
					continue;
				}
				
				if (field.getType() != type)
					continue;
				
				field.setAccessible(true);
				break;
			}
			
			if (field == null)
				field = NULL_FIELD;
			
			Field previous = cache.putIfAbsent(cls, field);
			if (previous != null)
				field = previous;
		}
		
		return (field != NULL_FIELD ? field : null);
	}
	
	private static PropertyAccessor findPropertyAccessor(Class<?> cls, ConcurrentMap<Class<?>, PropertyAccessor> cache, Class<? extends Annotation> annotationClass) {
		
		PropertyAccessor accessor = cache.get(cls);
		
		if (accessor == null) {
			boolean searchFields = false;
			boolean searchMethods = false;
			
			if (!annotationClass.isAnnotationPresent(Target.class))
				searchFields = searchMethods = true;
			else {
				Target target = annotationClass.getAnnotation(Target.class);
				for (ElementType targetType : target.value()) {
					if (targetType == ElementType.FIELD)
						searchFields = true;
					else if (targetType == ElementType.METHOD)
						searchMethods = true;
				}
			}
			
			if (searchFields == false && searchMethods == false)
				return null;
			
			final int modifierMask = Modifier.PUBLIC | Modifier.STATIC;
			
			classLoop:
			for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
				if (searchMethods) {
					for (Method method : c.getDeclaredMethods()) {
						if ((method.getModifiers() & modifierMask) != Modifier.PUBLIC ||
							!method.isAnnotationPresent(annotationClass))
							continue;
						
						if (method.getReturnType() == Void.TYPE) {
							if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
								String name = method.getName().substring(3);
								
								if (name.length() == 0)
									continue;
								
								Method getter = null;
								try {
									getter = cls.getMethod("get" + name);
								}
								catch (Exception e) {
									try {
										getter = cls.getMethod("is" + name);
									}
									catch (Exception f) {
									}
								}
								
								if (getter != null && (getter.getModifiers() & Modifier.STATIC) != 0 &&
									getter.getReturnType() != method.getParameterTypes()[0])
									getter = null;
								
								accessor = new MethodAccessor(getter, method, uncapitalizePropertyName(name));
								break classLoop;
							}
						}
						else if (method.getParameterTypes().length == 0 && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
							String name;
							if (method.getName().startsWith("get"))
								name = method.getName().substring(3);
							else
								name = method.getName().substring(2);
							
							if (name.length() == 0)
								continue;
							
							Method setter = null;
							try {
								setter = cls.getMethod("set" + name);
							}
							catch (Exception e) {
							}
							
							if (setter != null && (setter.getModifiers() & Modifier.STATIC) != 0 &&
								method.getReturnType() != setter.getParameterTypes()[0])
								setter = null;
							
							accessor = new MethodAccessor(method, setter, uncapitalizePropertyName(name));
							break classLoop;
						}
					}
				}
				
				if (searchFields) {
					for (Field field : c.getDeclaredFields()) {
						if ((field.getModifiers() & Modifier.STATIC) == 0 && field.isAnnotationPresent(annotationClass)) {
							accessor = new FieldAccessor(field);
							break classLoop;
						}
					}
				}
			}
			
			if (accessor == null)
				accessor = NULL_PROPERTY_ACCESSOR;
			
			PropertyAccessor previous = cache.putIfAbsent(cls, accessor);
			if (previous != null)
				accessor = previous;
		}
		
		return (accessor != NULL_PROPERTY_ACCESSOR ? accessor : null);
	}
	
	private static String uncapitalizePropertyName(String name) {
		if (name.length() > 0 && Character.isUpperCase(name.charAt(0)))
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
		return name;
	}
	
	static interface PropertyAccessor {
		
		String getName();
		Class<?> getType();
		
		boolean isReadable();
		boolean isWritable();
		
		Object get(Object obj) throws IllegalAccessException;
		void set(Object obj, Object value) throws IllegalAccessException;
	}
	
	static class FieldAccessor implements PropertyAccessor {

		private final Field field;
		
		public FieldAccessor(Field field) {
			field.setAccessible(true);

			this.field = field;
		}

		public Field getField() {
			return field;
		}

		public String getName() {
			return field.getName();
		}

		public Class<?> getType() {
			return field.getType();
		}

		public boolean isReadable() {
			return true;
		}

		public boolean isWritable() {
			return true;
		}

		public Object get(Object obj) throws IllegalAccessException {
			try {
				return field.get(obj);
			}
			catch (IllegalAccessException e) {
				throw e;
			}
			catch (Exception e) {
				throw new IllegalAccessException("Could not get field " + field + " value on object " + obj + ": " + e.toString());
			}
		}

		public void set(Object obj, Object value) throws IllegalAccessException {
			try {
				field.set(obj, value);
			}
			catch (IllegalAccessException e) {
				throw e;
			}
			catch (Exception e) {
				throw new IllegalAccessException("Could not set field " + field + " on object " + obj + " to value " + value + ": " + e.toString());
			}
		}
	}
	
	static class MethodAccessor implements PropertyAccessor {

		private final Method getter;
		private final Method setter;
		
		private final String name;

		public MethodAccessor(Method getter, Method setter, String name) {
			
			if (getter != null)
				getter.setAccessible(true);
			if (setter != null)
				setter.setAccessible(true);
			
			this.getter = getter;
			this.setter = setter;
			
			this.name = name;
		}

		public Method getGetter() {
			return getter;
		}

		public Method getSetter() {
			return setter;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return (getter != null ? getter.getReturnType() : setter.getParameterTypes()[0]);
		}

		public boolean isReadable() {
			return getter != null;
		}

		public boolean isWritable() {
			return setter != null;
		}

		public Object get(Object obj) throws IllegalAccessException {
			try {
				return getter.invoke(obj);
			}
			catch (IllegalAccessException e) {
				throw e;
			}
			catch (Exception e) {
				throw new IllegalAccessException("Could not invoke getter " + getter + " on object " + obj + ": " + e.toString());
			}
		}

		public void set(Object obj, Object value) throws IllegalAccessException {
			try {
				setter.invoke(obj, value);
			}
			catch (IllegalAccessException e) {
				throw e;
			}
			catch (Exception e) {
				throw new IllegalAccessException("Could not invoke setter " + setter + " on object " + obj + " with " + value + ": " + e.toString());
			}
		}
	}
	
	public static class Property {
		
		private final Object obj;
		private final PropertyAccessor accessor;

		public Property(Object obj, PropertyAccessor accessor) {
			this.obj = obj;
			this.accessor = accessor;
		}
		
		public String getName() {
			return accessor.getName();
		}
		
		public Class<?> getType() {
			return accessor.getType();
		}

		public boolean isReadable() {
			return accessor.isReadable();
		}

		public boolean isWritable() {
			return accessor.isWritable();
		}

		public Object getValue() throws IllegalAccessException {
			return accessor.get(obj);
		}

		public void setValue(Object value) throws IllegalAccessException {
			accessor.set(obj, value);
		}
	}
}
