package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.granite.client.javafx.JavaFXObject;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.javafx.PersistentList;
import org.granite.messaging.amf.RemoteClass;


@JavaFXObject
@Entity
@RemoteClass("org.granite.client.test.javafx.Entity1b")
public class FXEntity1b implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean __initialized = true;
    @SuppressWarnings("unused")
	private String __detachedState = null;
    
    public boolean isInitialized() {
        return __initialized;
    }
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);	
	private ObservableList<FXEntity2b> list = new PersistentList<FXEntity2b>();
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObservableList<FXEntity2b> getList() {
		return list;
	}
	public void setList(ObservableList<FXEntity2b> list) {
		this.list = list;
	}
}
