package org.granite.client.test.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.persistence.Version;

public abstract class Versioned implements Externalizable {

	private static final long serialVersionUID = 1L;

	@Version
    private Integer version;

    public Integer getVersion() {
        return version;
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		version = (Integer)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(version);
	}
}
