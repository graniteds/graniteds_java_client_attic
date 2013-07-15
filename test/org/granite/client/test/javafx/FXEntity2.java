package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.javafx.JavaFXObject;
import org.granite.client.messaging.RemoteAlias;


@JavaFXObject
@RemoteAlias("org.granite.client.test.javafx.Entity2")
public class FXEntity2 implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private boolean __initialized = true;
    @SuppressWarnings("unused")
	private String __detachedState = null;
    
    public boolean isInitialized() {
        return __initialized;
    }
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	private ObjectProperty<FXEntity1> entity1 = new SimpleObjectProperty<FXEntity1>(this, "entity1", null);
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<FXEntity1> entity1Property() {
		return entity1;
	}
	public FXEntity1 getEntity1() {
		return entity1.get();
	}
	public void setEntity1(FXEntity1 entity1) {
		this.entity1.set(entity1);
	}
}
