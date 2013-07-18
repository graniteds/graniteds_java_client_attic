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

package org.granite.client.tide.javafx;

import java.lang.reflect.Method;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;


public class ManagedEntityProperty<T> extends SimpleObjectProperty<T> {

	private EntityManager entityManager;
	private JavaFXDataManager dataManager;
	
	private BooleanProperty saved = new SimpleBooleanProperty(this, "saved", true);
	private BooleanProperty dirty = new SimpleBooleanProperty(this, "dirty", false);
	
	public ManagedEntityProperty(EntityManager entityManager, JavaFXDataManager dataManager) {
		super();
		init(entityManager, dataManager);
	}
	
	public ManagedEntityProperty(EntityManager entityManager, JavaFXDataManager dataManager, T value) {
		super(value);
		init(entityManager, dataManager);
	}
	
	public ManagedEntityProperty(EntityManager entityManager, JavaFXDataManager dataManager, Object bean, String property) {
		super(bean, property);
		init(entityManager, dataManager);
	}
	
	public ManagedEntityProperty(EntityManager entityManager, JavaFXDataManager dataManager, Object bean, String property, T value) {
		super(bean, property, value);
		init(entityManager, dataManager);
	}
	
	public BooleanProperty savedProperty() {
		return saved;
	}
	public boolean isSaved() {
		return saved.get();
	}
	
	public BooleanProperty dirtyProperty() {
		return dirty;
	}
	public boolean isDirty() {
		return dirty.get();
	}
	
	private ChangeListener<Object> entityChangeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
			if (oldValue != null) {
				ReadOnlyProperty<Object> versionProperty = getVersionProperty(oldValue);
				versionProperty.removeListener(versionChangeListener);
				dirty.unbind();
			}
			
			if (newValue == null)
				return;
			
			ReadOnlyProperty<Object> versionProperty = getVersionProperty(newValue);
			versionProperty.addListener(versionChangeListener);
			saved.set(versionProperty.getValue() != null);
			
			EntityManager entityManager = PersistenceManager.getEntityManager(newValue);
			if (entityManager == null)
				ManagedEntityProperty.this.entityManager.mergeExternalData(newValue);				
			else if (entityManager != ManagedEntityProperty.this.entityManager)
				throw new RuntimeException("Entity " + newValue + " cannot be attached: already attached to another entity manager");
			
			dirty.bind(dataManager.deepDirtyEntity(newValue));
		}
	};
	
	private ChangeListener<Object> versionChangeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
			saved.set(newValue != null);
		}
	};
	
	private void init(EntityManager entityManager, JavaFXDataManager dataManager) {
		this.entityManager = entityManager;
		this.dataManager = dataManager;		
		this.addListener(entityChangeListener);
	}
	
	public void reset() {
		if (get() == null)
			return;
		entityManager.resetEntity(get());
	}
	
	@SuppressWarnings("unchecked")
	private ReadOnlyProperty<Object> getVersionProperty(Object value) {
		String versionPropertyName = dataManager.getVersionPropertyName(value);
		if (versionPropertyName == null)
			throw new RuntimeException("No version property found on entity " + value);
		try {
			Method m = value.getClass().getMethod(versionPropertyName + "Property");
			return (ReadOnlyProperty<Object>)m.invoke(value);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not get version property on entity " + value, e);
		}		
	}
}
