package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@Entity
@RemoteAlias("org.granite.client.test.javafx.Entity3")
public class FXEntity3 implements Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private boolean __initialized__ = true;
	@SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private ReadOnlyIntegerWrapper id = new ReadOnlyIntegerWrapper(this, "id", 0);
	private StringProperty name = new SimpleStringProperty(this, "name", null);	
	
	public ReadOnlyIntegerProperty idProperty() {
		return id.getReadOnlyProperty();
	}
	public Integer getId() {
		return id.get();
	}
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String value) {
		name.set(value);
	}
}
