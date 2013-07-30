package org.granite.client.test.javafx;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Entity3 implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private Integer id;
	@Basic
	private String name;

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
