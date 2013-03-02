package org.granite.client.test.javafx;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Entity1c {
	
	@Basic
	private String name;
	
	@OneToMany(mappedBy="entity1")
	private Map<String, Entity2c> map = new HashMap<String, Entity2c>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, Entity2c> getMap() {
		return map;
	}
	public void setMap(Map<String, Entity2c> map) {
		this.map = map;
	}
}
