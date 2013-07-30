package org.granite.client.test.jmf;

public class ConcretePersitableChild extends AbstractPersistable<Integer> {

	private static final long serialVersionUID = 1L;

	private String a;
	private String z;
	
	public ConcretePersitableChild() {
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
}
