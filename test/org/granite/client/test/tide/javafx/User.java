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

import java.util.UUID;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.tide.data.Id;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;


public class User implements Identifiable, Lazyable {

    private boolean __initialized = true;
    @SuppressWarnings("unused")
    private String __detachedState = null;

    private final StringProperty username;
    private final StringProperty name;
    
    public StringProperty usernameProperty() {
        return username;
    }
    
    @Id
    public String getUsername() {
        return username.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    @Override
    public String getUid() {
        if (username.get() == null)
            return UUID.randomUUID().toString();
        return "User::" + username.get();
    }

    @Override
    public void setUid(String uid) {
    }

    public User() {        
        this.username = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
    }
    
    public User(String username, boolean initialized) {
        if (initialized) {
            this.username = new SimpleStringProperty(this, "username");
            this.name = new SimpleStringProperty(this, "name");
        }
        else {
            this.username = new ReadOnlyStringWrapper(this, "username", username);
            this.name = new SimpleStringProperty(this, "name");
            __initialized = false;
            __detachedState = "__SomeDetachedState__";
        }
    }
    
    public User(String username) {
        this.username = new ReadOnlyStringWrapper(this, "username", username);
        this.name = new SimpleStringProperty(this, "name");
    }

    @Override
    public boolean isInitialized() {
        return __initialized;
    }
}
