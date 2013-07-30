/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.test.tide.javafx;

import java.io.Serializable;
import java.util.UUID;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;


@Entity
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
	private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;

    @Id
    private final ReadOnlyObjectWrapper<Long> id = new ReadOnlyObjectWrapper<Long>(this, "id", null);
    @Version
    private final ReadOnlyObjectWrapper<Long> version = new ReadOnlyObjectWrapper<Long>(this, "version", null);
    @Uid
    private final StringProperty uid = new SimpleStringProperty(this, "uid", null);
    
    
    public AbstractEntity() {        
        this.uid.set(UUID.randomUUID().toString().toUpperCase());
    }
    
    public AbstractEntity(Long id, boolean initialized, String detachedState) {
        if (initialized) {
            this.uid.set(UUID.randomUUID().toString().toUpperCase());
        }
        else {
            this.id.set(id);
            __initialized__ = false;
            __detachedState__ = detachedState;
        }
    }
    
    public AbstractEntity(Long id, Long version, String uid) {
        this.id.set(id);
        this.version.set(version);
        this.uid.set(uid);
    }
    
    public ReadOnlyObjectProperty<Long> idProperty() {
        return id.getReadOnlyProperty();
    }        
    public Long getId() {
        return id.get();
    }
    
    public ReadOnlyObjectProperty<Long> versionProperty() {
        return version.getReadOnlyProperty();
    }        
    public Long getVersion() {
        return version.get();
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
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getId() + ":" + getVersion() + ":" + getUid();
    }
}
