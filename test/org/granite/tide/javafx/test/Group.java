package org.granite.tide.javafx.test;

import java.util.UUID;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.tide.data.Id;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;


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
