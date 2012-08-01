package org.granite.client.test.tide.javafx;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.Lazy;


public class Person extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private ObjectProperty<Salutation> salutation = new SimpleObjectProperty<Salutation>(this, "salutation");
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObservableList<Contact> contacts = null;
    
    
    public Person() {
        super();
    }
    
    public Person(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public Person(Long id, boolean initialized) {
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
    public ObservableList<Contact> getContacts() {
        return contacts;
    }
    
    public void setContacts(ObservableList<Contact> contacts) {
        this.contacts = contacts;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        super.readExternal(input);
        
        if (isInitialized()) {
            this.contacts = (ObservableList<Contact>)input.readObject();
            this.firstName.setValue((String)input.readObject());
            this.lastName.setValue((String)input.readObject());
            this.salutation.setValue((Salutation)input.readObject());
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        super.writeExternal(output);
        
        if (isInitialized()) {
            output.writeObject(((PropertyHolder)this.contacts).getObject());
            output.writeObject(firstName.getValue());
            output.writeObject(lastName.getValue());
            output.writeObject(salutation.getValue());
        }
    }
}
