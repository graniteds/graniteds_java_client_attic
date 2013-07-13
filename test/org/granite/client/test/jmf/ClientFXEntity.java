package org.granite.client.test.jmf;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;

@Entity
@RemoteAlias("org.granite.client.test.jmf.ServerEntity")
public class ClientFXEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private boolean __initialized__ = true;
	@SuppressWarnings("unused")
	private String __detachedState__ = null;
	
	@Id
	private ReadOnlyObjectWrapper<Integer> id = new ReadOnlyObjectWrapper<Integer>(this, "id", null);
	
	@Uid
	private StringProperty uid = new SimpleStringProperty(this, "uid", null);
	
	@Version
	private ReadOnlyObjectWrapper<Integer> version = new ReadOnlyObjectWrapper<Integer>(this, "version", null);

	private StringProperty name = new SimpleStringProperty(this, "name", null);
	
	private ReadOnlyListWrapper<ClientFXCollectionEntity> list = FXPersistentCollections.readOnlyObservablePersistentList(this, "list");

	public ClientFXEntity() {
	}

	public ClientFXEntity(Integer id, Integer version) {
		this.id.set(id);
		this.version.set(version);
	}

	public ReadOnlyObjectProperty<Integer> idProperty() {
		return id.getReadOnlyProperty();
	}
	public Integer getId() {
		return id.get();
	}

	public StringProperty uidProperty() {
		return uid;
	}
	public String getUid() {
		return uid.get();
	}
	public void setUid(String uid) {
		this.uid.set(uid);
	}

	public ReadOnlyObjectProperty<Integer> versionProperty() {
		return version.getReadOnlyProperty();
	}
	public Integer getVersion() {
		return version.get();
	}

	public StringProperty nameProperty() {
		return uid;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}

	public ReadOnlyListProperty<ClientFXCollectionEntity> listProperty() {
		return list.getReadOnlyProperty();
	}
	public ObservableList<ClientFXCollectionEntity> getList() {
		return list.get();
	}
}
