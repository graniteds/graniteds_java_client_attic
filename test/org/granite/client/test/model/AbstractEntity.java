package org.granite.client.test.model;

import java.util.UUID;

import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;

public abstract class AbstractEntity extends Versioned {

	private static final long serialVersionUID = 1L;

	private boolean __initialized__ = true;
	@SuppressWarnings("unused")
	private String __detachedState__ = null;
	
	@Id
	private Integer id;
	@Uid
	private String uid;
    private boolean restricted;

    public boolean is__initialized__() {
		return __initialized__;
	}

	public Integer getId() {
        return id;
    }
    
    public boolean isRestricted() {
    	return restricted;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this || (o instanceof AbstractEntity && uid().equals(((AbstractEntity)o).uid())));
    }

    @Override
    public int hashCode() {
        return uid().hashCode();
    }

    private String uid() {
        if (uid == null)
            uid = UUID.randomUUID().toString();
        return uid;
    }
}
