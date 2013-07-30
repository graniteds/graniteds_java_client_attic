package org.granite.client.test.javafx;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Entity1c implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Basic
	private String name;
	
	@Basic
	@Column(scale = 2, nullable = false)
	private BigDecimal value;
	
	@Basic
	@Column(scale = 2, nullable = false)
	private BigDecimal value2;

	
	@OneToMany(mappedBy="entity1")
	private Map<String, Entity2c> map = new HashMap<String, Entity2c>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	
	public BigDecimal getValue2() {
		return value2;
	}
	public void setValue2(BigDecimal value) {
		this.value2 = value;
	}
	
	public Map<String, Entity2c> getMap() {
		return map;
	}
	public void setMap(Map<String, Entity2c> map) {
		this.map = map;
	}
}
