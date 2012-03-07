package org.granite.tide.javafx.test;

import java.util.UUID;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.tide.data.Id;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;


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
