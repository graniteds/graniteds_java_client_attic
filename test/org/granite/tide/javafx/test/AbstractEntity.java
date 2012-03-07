package org.granite.tide.javafx.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.tide.data.Id;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;
import org.granite.tide.data.Version;


public class AbstractEntity implements Identifiable, Lazyable, Externalizable {

    private static final long serialVersionUID = 1L;
    
    private boolean __initialized = true;
    private String __detachedState = null;

    private final ObjectProperty<Long> id;
    private final ObjectProperty<Long> version;
    private final StringProperty uid;
    private final BooleanProperty dirty = new SimpleBooleanProperty(this, "dirty", false);

    
    public AbstractEntity() {        
        this.id = new SimpleObjectProperty<Long>(this, "id");
        this.version = new SimpleObjectProperty<Long>(this, "version");
        this.uid = new SimpleStringProperty(this, "uid", UUID.randomUUID().toString().toUpperCase());
    }
    
    public AbstractEntity(Long id, boolean initialized) {
        if (initialized) {
            this.id = new SimpleObjectProperty<Long>(this, "id");
            this.version = new SimpleObjectProperty<Long>(this, "version");
            this.uid = new SimpleStringProperty(this, "uid", UUID.randomUUID().toString().toUpperCase());
        }
        else {
            this.id = new ReadOnlyObjectWrapper<Long>(this, "id", id);
            this.version = new ReadOnlyObjectWrapper<Long>(this, "version");
            this.uid = new SimpleStringProperty(this, "uid");
            __initialized = false;
            __detachedState = "__SomeDetachedState__";
        }
    }
    
    public AbstractEntity(Long id, Long version, String uid) {
        this.id = new ReadOnlyObjectWrapper<Long>(this, "id", id);
        this.version = new ReadOnlyObjectWrapper<Long>(this, "version", version);
        this.uid = new ReadOnlyStringWrapper(this, "uid", uid);
    }
    
    public ObjectProperty<Long> idProperty() {
        return id;
    }
    
    @Id
    public Long getId() {
        return id.get();
    }
    
    public ObjectProperty<Long> versionProperty() {
        return version;
    }
    
    @Version
    public Long getVersion() {
        return version.get();
    }
    
    public StringProperty uidProperty() {
        return uid;
    }
    
    @Override
    public String getUid() {
        return uid.get();
    }

    @Override
    public void setUid(String uid) {
        this.uid.set(uid);
    }
    
    public BooleanProperty dirtyProperty() {
        return dirty;
    }
    
    public boolean isDirty() {
        return dirty.get();
    }

    @Override
    public boolean isInitialized() {
        return __initialized;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getId() + ":" + getVersion() + ":" + getUid();
    }
    
    
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        __initialized = input.readBoolean();
        __detachedState = (String)input.readObject();
        if (isInitialized()) {
            this.id.setValue((Long)input.readObject());
            this.uid.setValue((String)input.readObject());
            this.version.setValue((Long)input.readObject());
        }
        else {
            this.id.setValue((Long)input.readObject());
        }
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeBoolean(__initialized);
        output.writeObject(__detachedState);
        if (isInitialized()) {
            output.writeObject(this.id.getValue());
            output.writeObject(this.uid.getValue());
            output.writeObject(this.version.getValue());
        }
        else {
            output.writeObject(this.id.getValue());
        }
    }
}
