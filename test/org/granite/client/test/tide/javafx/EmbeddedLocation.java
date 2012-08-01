package org.granite.client.test.tide.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EmbeddedLocation {
    
    private StringProperty city = new SimpleStringProperty(this, "city");
    private StringProperty zipcode = new SimpleStringProperty(this, "zipcode");
    
    public EmbeddedLocation() {            
    }
    
    public EmbeddedLocation(String city, String zipcode) {
        this.city.set(city);
        this.zipcode.set(zipcode);
    }
    
    public StringProperty cityProperty() {
        return city;
    }
    
    public String getCity() {
        return city.get();
    }
    
    public void setCity(String city) {
        this.city.set(city);
    }
    
    public StringProperty zipcodeProperty() {
        return zipcode;
    }
    
    public String getZipcode() {
        return zipcode.get();
    }
    
    public void setZipcode(String zipcode) {
        this.zipcode.set(zipcode);
    }
}