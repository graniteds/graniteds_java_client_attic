package org.granite.tide.javafx.test;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class PersonEmbedNested extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObjectProperty<EmbeddedAddress2> address = new SimpleObjectProperty<EmbeddedAddress2>(this, "address");
    private ObservableList<Contact> contacts = null;
    
    
    public PersonEmbedNested() {
        super();
    }
    
    public PersonEmbedNested(Long id, Long version, String uid, String firstName, String lastName) {
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
    
    public ObjectProperty<EmbeddedAddress2> addressProperty() {
        return address;
    }
    
    public EmbeddedAddress2 getAddress() {
        return address.get();
    }
    
    public void setAddress(EmbeddedAddress2 address) {
        this.address.set(address);
    }
    
    public ObservableList<Contact> getContacts() {
        return contacts;
    }
    
    public void setContacts(ObservableList<Contact> contacts) {
        this.contacts = contacts;
    }
}
