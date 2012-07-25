package org.granite.javafx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;

import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.Property;
import org.granite.util.TypeUtil;
import org.granite.util.TypeUtil.DeclaredAnnotation;

public class JavaFXProperty extends Property {
	
	private final Method property;
	private final Method setter;
	private final Method getter;
	
    public JavaFXProperty(Converters converters, String name, Method property, Method getter, Method setter) {
        super(converters, name);
        this.property = property;
        this.setter = setter;
        this.getter = getter;
    }

	@SuppressWarnings("unchecked")
	@Override
	public void setProperty(Object instance, Object value, boolean convert) {
		Object convertedValue = convert ? convert(value) : value;
		try {
			((WritableValue<Object>)property.invoke(instance)).setValue(convertedValue);
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not set value of property " + property, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getProperty(Object instance) {
		try {
			return ((ObservableValue<Object>)property.invoke(instance)).getValue();
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not get value of property " + property, e);
		}
	}

	@Override
	public Type getType() {
		if (property.getGenericReturnType() instanceof ParameterizedType && ((ParameterizedType)property.getGenericReturnType()).getActualTypeArguments().length == 1)
			return ((ParameterizedType)property.getGenericReturnType()).getActualTypeArguments()[0];
		if (getter != null)
			return getter.getGenericReturnType();
		if (setter != null)
			return setter.getGenericParameterTypes()[0];
		
		throw new RuntimeException("Could not determine property type for property " + getName());
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, boolean recursive) {
		if (property != null) {
            if (property.isAnnotationPresent(annotationClass))
                return true;
            if (recursive && TypeUtil.isAnnotationPresent(property, annotationClass))
            	return true;
		}
        if (getter != null) {
            if (getter.isAnnotationPresent(annotationClass))
                return true;
            if (recursive && TypeUtil.isAnnotationPresent(getter, annotationClass))
            	return true;
        }
        if (setter != null) {
            if (setter.isAnnotationPresent(annotationClass))
            	return true;
            if (recursive && TypeUtil.isAnnotationPresent(setter, annotationClass))
            	return true;
        }
        return false;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, boolean recursive) {
    	T annotation = null;
    	if (property != null) {
    		annotation = property.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(property, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
    	if (getter != null) {
    		annotation = getter.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(getter, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
    	if (annotation == null && setter != null) {
    		annotation = setter.getAnnotation(annotationClass);
    		if (annotation == null && recursive) {
    			DeclaredAnnotation<T> declaredAnnotation = TypeUtil.getAnnotation(setter, annotationClass);
    			if (declaredAnnotation != null)
    				annotation = declaredAnnotation.annotation;
    		}
    	}
		return null;
	}

}
