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

package org.granite.client.javafx;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ObservableValue;

import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.Id;
import org.granite.client.util.BeanUtil;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.FieldProperty;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.granite.messaging.amf.io.util.externalizer.annotation.IgnoredProperty;
import org.granite.messaging.service.annotations.IgnoredMethod;

/**
 * @author William DRAI
 */
public class JavaFXExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(JavaFXExternalizer.class);
	

    public List<Property> findOrderedFields(final Class<?> clazz, boolean returnSettersWhenAvailable) {
        List<Property> fields = orderedFields.get(clazz);

        if (fields == null) {
            PropertyDescriptor[] propertyDescriptors = BeanUtil.getProperties(clazz);
            Converters converters = GraniteContext.getCurrentInstance().getGraniteConfig().getConverters();

            fields = new ArrayList<Property>();

            Set<String> allFieldNames = new HashSet<String>();
            allFieldNames.add("__initialized");
            allFieldNames.add("__detachedState");
            allFieldNames.add("__dirty");
            allFieldNames.add("__handlerManager");
            
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {

                List<Property> newFields = new ArrayList<Property>();
                
                for (Method method : c.getDeclaredMethods()) {
                	if (method.getName().endsWith("Property") && method.getParameterTypes().length == 0 && ObservableValue.class.isAssignableFrom(method.getReturnType())) {
                		String propertyName = method.getName().substring(0, method.getName().length()-8);
                		if (!allFieldNames.contains(propertyName) &&
                            !Modifier.isTransient(method.getModifiers()) &&
                            !Modifier.isStatic(method.getModifiers()) &&
                            !isPropertyIgnored(method) &&
                            !method.isAnnotationPresent(IgnoredMethod.class)) {
                			
                			PropertyDescriptor pdesc = null;
                			for (PropertyDescriptor pd : propertyDescriptors) {
                				if (pd.getName().equals(propertyName)) {
                					pdesc = pd;
                					break;
                				}
                			}                			
            				newFields.add(new JavaFXProperty(converters, propertyName, method, pdesc != null ? pdesc.getReadMethod() : null, pdesc != null ? pdesc.getWriteMethod() : null));
                            allFieldNames.add(propertyName);
                		}
                	}
                }

                // Standard declared fields.
                for (Field field : c.getDeclaredFields()) {
                    if (!allFieldNames.contains(field.getName()) &&
                        !Modifier.isTransient(field.getModifiers()) &&
                        !Modifier.isStatic(field.getModifiers()) &&
                        !isPropertyIgnored(field) &&
                        !field.isAnnotationPresent(IgnoredProperty.class)) {
                    	
                    	boolean found = false;
                    	if (returnSettersWhenAvailable && propertyDescriptors != null) {
                    		for (PropertyDescriptor pd : propertyDescriptors) {
                    			if (pd.getName().equals(field.getName()) && pd.getWriteMethod() != null) {
                    				newFields.add(new MethodProperty(converters, field.getName(), pd.getWriteMethod(), pd.getReadMethod()));
                    				found = true;
                    				break;
                    			}
                    		}
                    	}
                		if (!found)
                    		newFields.add(new FieldProperty(converters, field));
                    }
                    allFieldNames.add(field.getName());
                }

                // Getter annotated  by @ExternalizedProperty.
                if (propertyDescriptors != null) {
                    for (PropertyDescriptor property : propertyDescriptors) {
                        Method getter = property.getReadMethod();
                        if (getter != null &&
                            getter.isAnnotationPresent(ExternalizedProperty.class) &&
                            getter.getDeclaringClass().equals(c) &&
                            !allFieldNames.contains(property.getName())) {

                            newFields.add(new MethodProperty(converters, property.getName(), null, getter));
                            allFieldNames.add(property.getName());
                        }
                    }
                }
                
                try {
                	Field f = c.getDeclaredField("__externalizedProperties");
                	f.setAccessible(true);
                	final List<String> externalizedProperties = Arrays.asList((String[])f.get(null));
	                Collections.sort(newFields, new Comparator<Property>() {
	                    public int compare(Property o1, Property o2) {
	                        return externalizedProperties.indexOf(o1.getName()) - externalizedProperties.indexOf(o2.getName());
	                    }
	                });
                }
                catch (NoSuchFieldException e) {
	                Collections.sort(newFields, new Comparator<Property>() {
	                    public int compare(Property o1, Property o2) {
	                        return o1.getName().compareTo(o2.getName());
	                    }
	                });
                }
                catch (Exception e) {
                	throw new RuntimeException("Could not get value of __externalizedProperties", e);
                }
                
                fields.addAll(0, newFields);
            }

            List<Property> previousFields = (returnSettersWhenAvailable ? orderedSetterFields : orderedFields).putIfAbsent(clazz, fields);
            if (previousFields != null)
                fields = previousFields;
        }

        return fields;
    }    
    
    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
    	
    	Field initializedField = null;
    	Field detachedStateField = null;
    	Class<?> clazz = o.getClass();
    	while (clazz != null && clazz != Object.class) {
    		try {
    			initializedField = clazz.getDeclaredField("__initialized");
    		}
    		catch (NoSuchFieldException e) {
    		}
    		try {
    			detachedStateField = clazz.getDeclaredField("__detachedState");
    		}
    		catch (NoSuchFieldException e) {
    		}
    		clazz = clazz.getSuperclass();
    	}
    	
    	boolean initialized = true;
    	
    	if (initializedField != null && detachedStateField != null) {
	        // Read initialized flag.
	        initialized = ((Boolean)in.readObject()).booleanValue();
	
	        // Read detachedState.
	        String detachedState = (String)in.readObject();
	        
	        initializedField.setAccessible(true);
	        initializedField.set(o, initialized);
	        detachedStateField.setAccessible(true);
	        detachedStateField.set(o, detachedState);
    	}
        
    	if (initialized) {
            List<Property> fields = findOrderedFields(o.getClass(), false);
            log.debug("Reading entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = in.readObject();
                field.setProperty(o, value, true);
            }
        }
    	else {
            List<Property> fields = findOrderedFields(o.getClass(), false);
            log.debug("Reading entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
            	if (!field.isAnnotationPresent(Id.class))
            		continue;
                Object value = in.readObject();
                field.setProperty(o, value, true);
            }    		
    	}
    }

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {
    	
    	boolean initialized = true;
    	String detachedState = null;

    	Field initializedField = null;
    	Field detachedStateField = null;
    	Class<?> clazz = o.getClass();
    	while (clazz != null && clazz != Object.class) {
    		try {
    			initializedField = clazz.getDeclaredField("__initialized");
    			initializedField.setAccessible(true);
    			initialized = initializedField.getBoolean(o);
    		}
    		catch (NoSuchFieldException e) {
    		}
    		try {
    			detachedStateField = clazz.getDeclaredField("__detachedState");
    			detachedStateField.setAccessible(true);
    			detachedState = (String)detachedStateField.get(o);
    		}
    		catch (NoSuchFieldException e) {
    		}
    		clazz = clazz.getSuperclass();
    	}
    	
    	if (initializedField != null && detachedStateField != null) {
    		if (!initialized) {
	        	// Write initialized flag.
	        	out.writeObject(Boolean.FALSE);
	        	// Write detachedState.
	        	out.writeObject(detachedState);
	        	
	        	for (Property field : findOrderedFields(o.getClass(), false)) {
	        		if (field.isAnnotationPresent(Id.class)) {
		            	// Write entity id.
		                out.writeObject(field.getProperty(o));
		                break;
	        		}
	        	}
	            return;
    		}
        	
        	// Write initialized flag.
        	out.writeObject(Boolean.TRUE);
        	// Write detachedState.
        	out.writeObject(detachedState);
    	}
    	
        // Externalize entity fields.
        List<Property> fields = findOrderedFields(o.getClass(), false);
        log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
        for (Property field : fields) {
            Object value = field.getProperty(o);
            
            if (value != null && value instanceof PropertyHolder)
            	value = ((PropertyHolder)value).getObject();
            
            if (isValueIgnored(value))
            	out.writeObject(null);
            else
            	out.writeObject(value);
        }
    }
}
