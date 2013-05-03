package org.granite.client.test.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@RemoteAlias("org.granite.example.addressbook.entity.Country")
@Entity
public class Country extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
	
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		if (is__initialized__())
			name = in.readUTF();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		if (is__initialized__())
			out.writeUTF(name);
	}
}
