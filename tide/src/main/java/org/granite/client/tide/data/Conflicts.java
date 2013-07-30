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

import java.util.ArrayList;
import java.util.List;

import org.granite.logging.Logger;

/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflicts {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.Conflicts");

    private EntityManager entityManager;
    
    private List<Conflict> conflicts = new ArrayList<Conflict>();
    


    public Conflicts(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public void addConflict(Object localEntity, Object receivedEntity, List<String> properties) {
        Conflict conflict = new Conflict(this, localEntity, receivedEntity, properties);
        conflicts.add(conflict);
    }
    
    public List<Conflict> getConflicts() {
        return conflicts;
    }        
    
    public boolean isEmpty() {
        return conflicts.size() == 0;
    }
    
    public boolean isAllResolved() {
        for (Conflict c : conflicts) {
            if (!c.isResolved())
                return false;
        }
        return true;
    }
    
    public void acceptClient(Conflict conflict) {
        entityManager.acceptConflict(conflict, true);
    }
    
    public void acceptAllClient() {
        for (Conflict c : conflicts)
            acceptClient(c);
    }
    
    public void acceptServer(Conflict conflict) {
        entityManager.acceptConflict(conflict, false);
    }
    
    public void acceptAllServer() {
        for (Conflict c : conflicts)
            acceptServer(c);
    }
}
