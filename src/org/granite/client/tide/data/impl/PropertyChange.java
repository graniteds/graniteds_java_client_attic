package org.granite.client.tide.data.impl;


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
