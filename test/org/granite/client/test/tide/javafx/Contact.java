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

import org.granite.client.tide.data.Lazy;

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
