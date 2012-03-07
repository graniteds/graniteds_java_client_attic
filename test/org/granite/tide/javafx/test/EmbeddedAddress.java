package org.granite.tide.javafx.test;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmbeddedAddress {
    
    private StringProperty address = new SimpleStringProperty(this, "address");
    
    public EmbeddedAddress() {            
    }
    
    public EmbeddedAddress(String address) {
        this.address.set(address);
    }
    
    public StringProperty addressProperty() {
        return address;
    }
    
    public String getAddress() {
        return address.get();
    }
    
    public void setAddress(String address) {
        this.address.set(address);
    }
}