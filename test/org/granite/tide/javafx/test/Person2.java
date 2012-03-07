package org.granite.tide.javafx.test;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class Person2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObservableList<String> names = null;
    private ObservableList<Contact> contacts = null;
    
    
    public Person2() {
        super();
    }
    
    public Person2(Long id, Long version, String uid, String firstName, String lastName) {
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
    
    public List<String> getNames() {
        return names;
    }
    
    public void setNames(ObservableList<String> names) {
        this.names = names;
    }
    
    public ObservableList<Contact> getContacts() {
        return contacts;
    }
    
    public void setContacts(ObservableList<Contact> contacts) {
        this.contacts = contacts;
    }
}
