package org.granite.client.test.javafx;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity
public class Entity2 implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Basic
	private String name;
	
	@ManyToOne
	private Entity1 entity1;
	
	public Entity1 getEntity1() {
		return entity1;
	}
	public void setEntity1(Entity1 entity1) {
		this.entity1 = entity1;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
