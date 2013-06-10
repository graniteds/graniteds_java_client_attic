package org.granite.client.test.javafx;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;


@Entity
public class Entity2c implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Basic
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
