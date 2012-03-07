package org.granite.tide.javafx.test;

import org.granite.tide.data.Lazy;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class PersonUniDir extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private ObjectProperty<Salutation> salutation = new SimpleObjectProperty<Salutation>(this, "salutation");
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObservableList<Contact2> contacts = null;
    
    
    public PersonUniDir() {
        super();
    }
    
    public PersonUniDir(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public PersonUniDir(Long id, boolean initialized) {
        super(id, initialized);
    }
    
    public ObjectProperty<Salutation> salutationProperty() {
        return salutation;
    }
    
    public Salutation getSalutation() {
        return salutation.get();
    }
    
    public void setSalutation(Salutation salutation) {
        this.salutation.set(salutation);
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
    
    @Lazy
    public ObservableList<Contact2> getContacts() {
        return contacts;
    }
    
    public void setContacts(ObservableList<Contact2> contacts) {
        this.contacts = contacts;
    }
}
