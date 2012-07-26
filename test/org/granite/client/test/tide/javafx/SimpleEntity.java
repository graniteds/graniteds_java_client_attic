package org.granite.client.test.tide.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class SimpleEntity extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty name = new SimpleStringProperty(this, "name");
    
    
    public SimpleEntity() {
        super();
    }
    
    public SimpleEntity(Long id, Long version, String uid, String name) {
        super(id, version, uid);
        this.name.set(name);
    }
    
    public SimpleEntity(Long id, boolean initialized) {
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
}
