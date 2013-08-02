/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.validation.javafx;

import javafx.beans.property.Property;

/**
 * @author William DRAI
 */
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
	
	public boolean isError() {
		return error;
	}

	public Property<?> getProperty() {
		return property;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
