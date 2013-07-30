package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.messaging.annotations.Serialized;


@Entity
@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity2b")
public class FXEntity2b implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	private ObjectProperty<FXEntity1b> entity1 = new SimpleObjectProperty<FXEntity1b>(this, "entity1", null);
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<FXEntity1b> entity1Property() {
		return entity1;
	}
	public FXEntity1b getEntity1() {
		return entity1.get();
	}
	public void setEntity1(FXEntity1b entity1) {
		this.entity1.set(entity1);
	}
}
