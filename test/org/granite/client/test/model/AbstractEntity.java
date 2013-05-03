package org.granite.client.test.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;

public abstract class AbstractEntity extends Versioned {

	private static final long serialVersionUID = 1L;

	private boolean __initialized__ = true;
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

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		__initialized__ = in.readBoolean();
		__detachedState__ = in.readUTF();
		
		if (!is__initialized__())
			id = (Integer)in.readObject();
		else {
			super.readExternal(in);
			id = (Integer)in.readObject();
			restricted = in.readBoolean();
			uid = in.readUTF();
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(__initialized__);
		out.writeUTF(__detachedState__);
		
		if (!is__initialized__())
			out.writeObject(id);
		else {
			super.writeExternal(out);
			out.writeObject(id);
			out.writeBoolean(restricted);
			out.writeUTF(uid);
		}
	}
}
