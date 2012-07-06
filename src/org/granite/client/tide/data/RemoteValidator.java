package org.granite.client.tide.data;


public interface RemoteValidator {
    
    public void setEnabled(boolean enabled);

    public boolean isEnabled();
    
    /**
     *  Trigger remote validation of an object
     *
     *  @param object the object to validate
     *  @param property the property to validate
     *  @param value the value to validate
     * 
     *  @return true if validation triggered
     */
    public boolean validateObject(Object object, String property, Object value);

}
