package org.granite.client.test.javafx;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Entity1b {
	
	@Basic
	private String name;
	
	@OneToMany(mappedBy="entity1")
	private Set<Entity2b> list = new HashSet<Entity2b>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<Entity2b> getList() {
		return list;
	}
	public void setList(Set<Entity2b> list) {
		this.list = list;
	}
}
