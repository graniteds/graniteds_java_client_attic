package org.granite.client.test.javafx;

import java.math.BigDecimal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import org.granite.client.javafx.JavaFXObject;
import org.granite.messaging.amf.RemoteClass;


@JavaFXObject
@RemoteClass("org.granite.client.test.javafx.Entity1c")
public class FXEntity1c {
	
    private boolean __initialized = true;
    @SuppressWarnings("unused")
	private String __detachedState = null;
    
    public boolean isInitialized() {
        return __initialized;
    }
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	private ObjectProperty<BigDecimal> value = new SimpleObjectProperty<BigDecimal>(this, "value", null);
	private ObjectProperty<BigDecimal> value2 = new SimpleObjectProperty<BigDecimal>(this, "value2", null);
	private ObservableMap<String, FXEntity2c> map = FXCollections.observableHashMap();
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<BigDecimal> valueProperty() {
		return value;
	}
	public BigDecimal getValue() {
		return this.value.get();
	}
	public void setValue(BigDecimal value) {
		this.value.set(value);
	}
	
	public ObjectProperty<BigDecimal> value2Property() {
		return value2;
	}
	public BigDecimal getValue2() {
		return this.value2.get();
	}
	public void setValue2(BigDecimal value) {
		this.value2.set(value);
	}
	
	public ObservableMap<String, FXEntity2c> getMap() {
		return map;
	}
	public void setMap(ObservableMap<String, FXEntity2c> map) {
		this.map = map;
	}
}
