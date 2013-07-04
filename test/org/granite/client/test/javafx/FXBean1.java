package org.granite.client.test.javafx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.granite.client.javafx.JavaFXObject;


@JavaFXObject
public class FXBean1 implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<String> list = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
}
