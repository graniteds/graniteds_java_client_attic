package org.granite.tide.javafx.test;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmbeddedAddress2 {
    
    private StringProperty address = new SimpleStringProperty(this, "address");
    private ObjectProperty<EmbeddedLocation> location = new SimpleObjectProperty<EmbeddedLocation>(this, "location");
    
    public EmbeddedAddress2() {            
    }
    
    public EmbeddedAddress2(String address) {
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
    
    public ObjectProperty<EmbeddedLocation> locationProperty() {
        return location;
    }
    
    public EmbeddedLocation getLocation() {
        return location.get();
    }
    
    public void setLocation(EmbeddedLocation location) {
        this.location.set(location);
    }
}