package org.granite.client.test.javafx;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Entity1 {
	
	@Basic
	private String name;
	
	@OneToMany(mappedBy="entity1")
	private List<Entity2> list = new ArrayList<Entity2>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Entity2> getList() {
		return list;
	}
	public void setList(List<Entity2> list) {
		this.list = list;
	}
}
