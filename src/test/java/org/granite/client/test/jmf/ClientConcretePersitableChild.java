package org.granite.client.test.jmf;

import java.io.Serializable;

import org.granite.client.messaging.RemoteAlias;
import org.granite.messaging.annotations.Serialized;

@RemoteAlias("org.granite.client.test.jmf.ConcretePersitableChild")
@Serialized(propertiesOrder={"id", "a", "z"})
public class ClientConcretePersitableChild implements Serializable {

	private static final long serialVersionUID = 1L;

	private String z;
	private String a;
	private Integer id;
	
	public ClientConcretePersitableChild() {
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getZ() {
		return z;
	}

	public void setZ(String z) {
		this.z = z;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
