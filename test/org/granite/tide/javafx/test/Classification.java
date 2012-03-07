package org.granite.tide.javafx.test;

import org.granite.tide.data.Lazy;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;


public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty name = new SimpleStringProperty(this, "name");
    private ObservableList<Classification> subclasses = null;
    private ObservableList<Classification> superclasses = null;
    
    
    public Classification() {
        super();
    }
    
    public Classification(Long id, Long version, String uid, String name) {
        super(id, version, uid);
        this.name.set(name);
    }
    
    public Classification(Long id, boolean initialized) {
        super(id, initialized);
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
    
    @Lazy
    public ObservableList<Classification> getSubclasses() {
        return subclasses;
    }
    
    public void setSubclasses(ObservableList<Classification> subclasses) {
        this.subclasses = subclasses;
    }
    
    @Lazy
    public ObservableList<Classification> getSuperclasses() {
        return superclasses;
    }
    
    public void setSuperclasses(ObservableList<Classification> superclasses) {
        this.superclasses = superclasses;
    }
}
