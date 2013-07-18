package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;
import org.granite.messaging.annotations.Serialized;


@Entity
@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity1b")
public class FXEntity1b implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);	
	private ReadOnlySetWrapper<FXEntity2b> list = FXPersistentCollections.readOnlyObservablePersistentSet(this, "list");
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ReadOnlySetProperty<FXEntity2b> listProperty() {
		return list.getReadOnlyProperty();
	}
	public ObservableSet<FXEntity2b> getList() {
		return list.get();
	}
}
