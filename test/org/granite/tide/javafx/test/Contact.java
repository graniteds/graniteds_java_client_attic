package org.granite.tide.javafx.test;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.tide.data.Lazy;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Contact extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private ObjectProperty<Person> person = new SimpleObjectProperty<Person>(this, "person");
    private StringProperty email = new SimpleStringProperty(this, "email");
    
    
    public Contact() {
        super();
    }
    
    public Contact(Long id, Long version, String uid, String email) {
        super(id, version, uid);
        this.email.set(email);
    }    
        
    public ObjectProperty<Person> personProperty() {
        return person;
    }
    
    @Lazy
    public Person getPerson() {
        return person.get();
    }
    
    public void setPerson(Person person) {
        this.person.set(person);
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public void setEmail(String email) {
        this.email.set(email);
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        super.readExternal(input);
        
        if (isInitialized()) {
            this.email.setValue((String)input.readObject());
            this.person.setValue((Person)input.readObject());
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        super.writeExternal(output);
        
        if (isInitialized()) {
            output.writeObject(this.email.getValue());
            output.writeObject(this.person.getValue());
        }
    }
}
