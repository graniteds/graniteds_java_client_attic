package org.granite.tide.data;


public interface DataConflictListener {

    public void onConflict(EntityManager entityManager, Conflicts conflicts);
}
