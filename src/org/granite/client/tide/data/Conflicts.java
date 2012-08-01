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
    
    public void addConflict(Identifiable localEntity, Identifiable receivedEntity) {
        Conflict conflict = new Conflict(this, localEntity, receivedEntity);
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
