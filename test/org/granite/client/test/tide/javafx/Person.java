/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

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
            output.writeObject(this.contacts != null ? ((PropertyHolder)this.contacts).getObject() : null);
            output.writeObject(firstName.getValue());
            output.writeObject(lastName.getValue());
            output.writeObject(salutation.getValue());
        }
    }
}
