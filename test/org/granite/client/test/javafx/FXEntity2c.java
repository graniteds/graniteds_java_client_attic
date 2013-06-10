package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.javafx.JavaFXObject;
import org.granite.messaging.amf.RemoteClass;


@JavaFXObject
@RemoteClass("org.granite.client.test.javafx.Entity2c")
public class FXEntity2c implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private boolean __initialized = true;
    @SuppressWarnings("unused")
	private String __detachedState = null;
    
    public boolean isInitialized() {
        return __initialized;
    }
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	
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
