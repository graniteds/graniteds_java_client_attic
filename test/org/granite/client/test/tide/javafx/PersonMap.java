package org.granite.client.test.tide.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;


public class PersonMap extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObservableMap<Integer, String> mapSimple = null;
    private ObservableMap<String, EmbeddedAddress> mapEmbed = null;
    private ObservableMap<String, SimpleEntity> mapEntity = null;
    
    
    public PersonMap() {
        super();
    }
    
    public PersonMap(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    
    public String getFirstName() {
        return firstName.get();
    }
    
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    
    public String getLastName() {
        return lastName.get();
    }
    
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    public ObservableMap<String, EmbeddedAddress> getMapEmbed() {
        return mapEmbed;
    }
    
    public void setMapEmbed(ObservableMap<String, EmbeddedAddress> map) {
        this.mapEmbed = map;
    }
    
    public ObservableMap<Integer, String> getMapSimple() {
        return mapSimple;
    }
    
    public void setMapSimple(ObservableMap<Integer, String> map) {
        this.mapSimple = map;
    }
    
    public ObservableMap<String, SimpleEntity> getMapEntity() {
        return mapEntity;
    }
    
    public void setMapEntity(ObservableMap<String, SimpleEntity> map) {
        this.mapEntity = map;
    }
}
