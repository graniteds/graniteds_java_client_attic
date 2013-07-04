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

package org.granite.client.tide.data;

import java.util.List;

import org.granite.logging.Logger;

/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflict {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(Conflict.class);

    private Conflicts conflicts;
    
    private Object localEntity;
    private Object receivedEntity;
    private List<String> properties;
    private boolean resolved = false;
    


    public Conflict(Conflicts conflicts, Object localEntity, Object receivedEntity, List<String> properties) {
        this.conflicts = conflicts;
        this.localEntity = localEntity;
        this.receivedEntity = receivedEntity;
        this.properties = properties;
    }
    
    public Object getLocalEntity() {
        return localEntity;
    }        
    
    public Object getReceivedEntity() {
        return receivedEntity;
    }
    
    public List<String> getProperties() {
    	return properties;
    }
    
    public boolean isRemoval() {
        return receivedEntity == null;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void acceptClient() {
        conflicts.acceptClient(this);
        resolved = true;
    }
    
    public void acceptServer() {
        conflicts.acceptServer(this);
        resolved = true;
    }
}
