package org.granite.client.test.javafx;

import javax.persistence.Basic;
import javax.persistence.Entity;


@Entity
public class Entity2c {
	
	@Basic
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
