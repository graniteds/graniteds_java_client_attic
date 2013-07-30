package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;
import org.granite.messaging.annotations.Serialized;


@Entity
@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity1")
public class FXEntity1 implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);	
	private ReadOnlyListWrapper<FXEntity2> list = FXPersistentCollections.readOnlyObservablePersistentList(this, "list");
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ReadOnlyListProperty<FXEntity2> listProperty() {
		return list.getReadOnlyProperty();
	}
	public ObservableList<FXEntity2> getList() {
		return list.get();
	}
}
