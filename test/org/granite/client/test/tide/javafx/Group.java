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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.tide.data.Id;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Lazyable;


public class Group implements Identifiable, Lazyable {

    private boolean __initialized = true;
    @SuppressWarnings("unused")
    private String __detachedState = null;

    private final StringProperty name;
    private final ObjectProperty<User> user;
    
    public StringProperty nameProperty() {
        return name;
    }
    
    @Id
    public String getName() {
        return name.get();
    }
    
    public ObjectProperty<User> userProperty() {
        return user;
    }
    
    public User getUser() {
        return user.get();
    }
    
    public void setUser(User user) {
        this.user.set(user);
    }
    
    @Override
    public String getUid() {
        if (name.get() == null)
            return UUID.randomUUID().toString();
        return "Group::" + name.get();
    }

    @Override
    public void setUid(String uid) {
    }

    public Group() {        
        this.name = new SimpleStringProperty(this, "name");
        this.user = new SimpleObjectProperty<User>(this, "user");
    }
    
    public Group(String name, boolean initialized) {
        if (initialized) {
            this.name = new SimpleStringProperty(this, "name");
            this.user = new SimpleObjectProperty<User>(this, "user");
        }
        else {
            this.name = new ReadOnlyStringWrapper(this, "name", name);
            this.user = new SimpleObjectProperty<User>(this, "user");
            __initialized = false;
            __detachedState = "__SomeDetachedState__";
        }
    }
    
    public Group(String name) {
        this.name = new ReadOnlyStringWrapper(this, "name", name);
        this.user = new SimpleObjectProperty<User>(this, "user");
    }

    @Override
    public boolean isInitialized() {
        return __initialized;
    }
}
