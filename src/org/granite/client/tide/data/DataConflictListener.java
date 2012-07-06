package org.granite.client.tide.data;



public interface DataConflictListener {

    public void onConflict(EntityManager entityManager, Conflicts conflicts);
}
