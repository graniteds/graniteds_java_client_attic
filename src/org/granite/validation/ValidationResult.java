package org.granite.validation;

import javafx.beans.property.Property;


public class ValidationResult {

	private boolean error;
	private Property<?> property;
	private String code;
	private String message;
	
	
	public ValidationResult(boolean error, Property<?> property, String code, String message) {
		this.error = error;
		this.property = property;
		this.code = code;
		this.message = message;
	}
}
