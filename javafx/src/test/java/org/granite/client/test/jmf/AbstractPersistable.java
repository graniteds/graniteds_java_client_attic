package org.granite.client.test.jmf;

import java.io.Serializable;

public class AbstractPersistable<PK extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

	private PK id;
	
	public AbstractPersistable() {
	}

	public PK getId() {
		return id;
	}

	public void setId(PK id) {
		this.id = id;
	}
}
