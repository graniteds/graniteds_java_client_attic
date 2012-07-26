package org.granite.client.tide.data;

import org.granite.logging.Logger;


/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflict {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.Conflict");

    private Conflicts conflicts;
    
    private Identifiable localEntity;
    private Identifiable receivedEntity;
    private boolean resolved = false;
    


    public Conflict(Conflicts conflicts, Identifiable localEntity, Identifiable receivedEntity) {
        this.conflicts = conflicts;
        this.localEntity = localEntity;
        this.receivedEntity = receivedEntity;
    }
    
    public Identifiable getLocalEntity() {
        return localEntity;
    }        
    
    public Identifiable getReceivedEntity() {
        return receivedEntity;
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
