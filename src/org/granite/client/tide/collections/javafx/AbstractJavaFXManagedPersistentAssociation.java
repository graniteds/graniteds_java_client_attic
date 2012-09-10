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

package org.granite.client.tide.collections.javafx;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.collections.ManagedPersistentAssociation;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.client.tide.data.spi.Wrapper;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public abstract class AbstractJavaFXManagedPersistentAssociation implements ManagedPersistentAssociation, PropertyHolder, Wrapper {
    
    private static Logger log = Logger.getLogger("org.granite.client.tide.javafx.AbstractJavaFXManagedPersistentAssociation");
    
    private final Identifiable entity;
    private final String propertyName;
    
    private ServerSession serverSession = null;

    private boolean localInitializing = false;
    private boolean initializing = false;
    private List<InitializationListener> listeners = new ArrayList<InitializationListener>();
    private InitializationCallback initializationCallback = null;
    
    
    public Identifiable getOwner() {
        return entity;
    } 
    
    public String getPropertyName() {
        return propertyName;
    }
    
    protected AbstractJavaFXManagedPersistentAssociation(Identifiable entity, String propertyName) {
        this.entity = entity;
        this.propertyName = propertyName;
    }
    
    public void setServerSession(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
    }
        
    public Object getWrappedObject() {
        return getObject();
    }
    
    public void propertyResultHandler(String propName, ResultEvent event) {
    }
    
    public void setProperty(String propertyName, Object value) {
    }
    
    public boolean isInitialized() {
        return ((LazyableCollection)getObject()).isInitialized();
    }
    
    public boolean isInitializing() {
        return initializing;
    }
    
    public void initializing() {
        ((LazyableCollection)getObject()).initializing();
        localInitializing = true;
    }
    
    public void addListener(InitializationListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    
    public void removeListener(InitializationListener listener) {
        listeners.remove(listener);
    }
    
    private void requestInitialization() {
        if (localInitializing)
            return;
        
        EntityManager entityManager = PersistenceManager.getEntityManager(entity);
        if (!initializing && entityManager.initializeObject(serverSession, this))                
            initializing = true;
    }
    
    protected boolean checkForRead() {
        return checkForRead(true);
    }
    protected boolean checkForRead(boolean requestInitialization) {
        if (!localInitializing && !isInitialized()) {
            if (requestInitialization)
                requestInitialization();
            return false;
        }
        return true;
    }
    protected void checkForWrite() {
        if (!localInitializing && !isInitialized())
            throw new IllegalStateException("Cannot modify uninitialized association: " + getOwner() + " property " + getPropertyName()); 
    }
    
    public void initialize() {
        ((LazyableCollection)getObject()).initialize();
        localInitializing = false;
        
        for (InitializationListener listener : listeners)
            listener.initialized(this);
        
        log.debug("initialized");
    }
    
    public void uninitialize() {
        ((LazyableCollection)getObject()).uninitialize();
        initializing = false;
        initializationCallback = null;
        localInitializing = false;
        
        for (InitializationListener listener : listeners)
            listener.uninitialized(this);
    }
    
    public void withInitialized(InitializationCallback callback) {
        if (isInitialized())
            initializationCallback.call(this);
        else {
            initializationCallback = callback;
            requestInitialization();
        }
    }
        
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + ObjectUtil.toString(entity) + "." + getPropertyName() + ": " + getObject().toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(getClass()))
            return false;
        
        AbstractJavaFXManagedPersistentAssociation mass = (AbstractJavaFXManagedPersistentAssociation)obj;
        return entity.equals(mass.entity) && propertyName.equals(mass.propertyName);
    }
    
    @Override
    public int hashCode() {
        int hashCode = entity.hashCode();
        hashCode = 37 * hashCode + propertyName.hashCode();
        return hashCode;
    }
}